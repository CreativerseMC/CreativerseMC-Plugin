// SPDX-License-Identifier: MIT
pragma solidity ^0.8.2;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Enumerable.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Royalty.sol";
import "erc20create.sol";

contract Plot is ERC721URIStorage, ERC721Enumerable, ERC721Royalty {

    Creativerse token;
    address gov;
    address admin;
    address owner; // To receive minting payments

    struct LockInfo {
        uint256 start;
        uint256 end;
        uint256 total;
        uint256 claimed;
    }

    mapping(uint256 => LockInfo) private tokenLockInfo; // Locking info associated with plot. After locking ends, plot owner should claim tokens before transferring plot, or they may lose their claim.

    mapping(uint256 => bool) private locked;
    uint256 rate = 64602;
    uint256 thirtyDays = 2592000; // 30 days in blocks

    constructor() ERC721("Creativerse Plot", "PLOT") {
        admin = msg.sender;
        owner = msg.sender;
    }

    function getTokenLockInfo(uint256 tokenId) public view returns (uint256, uint256, uint256, uint256) {
        return (tokenLockInfo[tokenId].start, tokenLockInfo[tokenId].end, tokenLockInfo[tokenId].total, tokenLockInfo[tokenId].claimed);
    }

    function isLocked(uint256 tokenId) public view returns (bool) {
        return locked[tokenId];
    }

    function doesExist(uint256 tokenId) public view returns (bool) {
        return _exists(tokenId);
    }

    event Edit(uint256 tokenId, string cid);

    function editPlot(uint256 tokenId, string memory cid) public payable returns (uint256) {
        if (_exists(tokenId) && ownerOf(tokenId) == msg.sender) {
            _setTokenURI(tokenId, cid);
        } else {
            require(tokenId <= 19800);
            require(msg.value == 5 ether); // Minting costs 5 matic
            payable(owner).transfer(5 ether);
            _safeMint(msg.sender, tokenId);
            _setTokenURI(tokenId, cid);
            locked[tokenId] = false;
        }
        emit Edit(tokenId, cid);
        
        return tokenId;
    }

    // time is in 30 day intervals; every 1 time = 2592000 blocks
    function lock(uint256 tokenId, uint256 time) external {
        require(locked[tokenId] == false);
        require(ownerOf(tokenId) == msg.sender);
        require(time <= 48); // No longer than 48 30-day periods, or around 4 years
        locked[tokenId] = true;
        LockInfo memory lockInfo = LockInfo(
            block.timestamp,
            block.timestamp + time*2592000,
            totalClaim(time),
            0
        );

        tokenLockInfo[tokenId] = lockInfo;
    }

    function unlock(uint256 tokenId) external {
        require(locked[tokenId] == true);
        require(tokenLockInfo[tokenId].end < block.timestamp);
        locked[tokenId] = false;
    }

    // Given a time period in terms of 30 day intervals, returns total reward by end of period. Cannot be more than 48, or around 4 years.
    function totalClaim(uint256 time) public view returns (uint256) {
        require(time <= 48);
        uint256 squared = (time*thirtyDays)**2;
        return squared*rate;
    }

    // Given a token ID, returns amount of tokens that can be claimed by plot owner
    function claimable(uint256 tokenId) public view returns (uint256) {
        uint256 deltaTotal = tokenLockInfo[tokenId].end - tokenLockInfo[tokenId].start;
        uint256 deltaCurrent = block.timestamp - tokenLockInfo[tokenId].start;
        uint256 tokensPerBlock = tokenLockInfo[tokenId].total / deltaTotal;

        uint256 claimableAmount = tokensPerBlock*deltaCurrent;
        if (claimableAmount > tokenLockInfo[tokenId].total) {
            claimableAmount = tokenLockInfo[tokenId].total;
        }

        return claimableAmount - tokenLockInfo[tokenId].claimed;
    }

    // Claim CREATE tokens 
    function claim(uint256 tokenId) public returns (uint256) {
        require(ownerOf(tokenId) == msg.sender);
        uint256 claimableAmount = claimable(tokenId);
        tokenLockInfo[tokenId].claimed += claimableAmount;
        token.mint(claimableAmount);
        token.transfer(msg.sender, claimableAmount);
        return claimableAmount;
    }

    // Will be used to set the admin to the 0x000... address once governance is deployed
    function setAdmin(address a) public {
        require(msg.sender == admin);
        admin = a;
    }

    // Can be changed by governance. numerator/10000 = royalty percent
    // Royalty will be paid out to governance
    function setRoyalty(address collector, uint96 numerator) external {
        require(msg.sender == gov || msg.sender == admin);
        _setDefaultRoyalty(collector, numerator);
    }

    function setCreateTokenContract(address tokenContract) external {
        require(msg.sender == gov || msg.sender == admin);
        token = Creativerse(tokenContract);
    }

    function setGovContract(address govContract) external {
        require(msg.sender == gov || msg.sender == admin);
        gov = govContract;
    }

    // Sets rate constant for token reward. More information can be found here: https://www.desmos.com/calculator/clofmn5mzu (rate is C in this)
    function setRate(uint256 r) external {
        require(msg.sender == gov || msg.sender == admin);
        rate = r;
    }

    // Required by Solidity

    function _beforeTokenTransfer(address from, address to, uint256 tokenId)
        internal
        override(ERC721, ERC721Enumerable)
    {
        require(locked[tokenId] == false); // Prevents locked token from being transferred
        super._beforeTokenTransfer(from, to, tokenId);
    }

    function _burn(uint256 tokenId)
        internal
        override(ERC721, ERC721URIStorage, ERC721Royalty)
    {
        super._burn(tokenId);
    }

    function supportsInterface(bytes4 interfaceId)
        public view
        override(ERC721, ERC721Enumerable, ERC721Royalty)
        returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }

    function tokenURI(uint256 tokenId)
        public view
        override(ERC721, ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }

    function _baseURI()
        internal view virtual
        override(ERC721)
        returns (string memory)
    {
        return "ipfs://";
    }
}
