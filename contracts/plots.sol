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

        // Giveaway winners & Early Testers
        _safeMint(0xD1BEcF2CFb20861b6a9d7A6AE9ec2C69Bd1543dC, 11);
        _setTokenURI(11, "bafkreigriulcnrd4vtcesvi7pbyu7wbq3jzr5ee6uzjairwvlb24dvknz4");
        _safeMint(0xD1BEcF2CFb20861b6a9d7A6AE9ec2C69Bd1543dC, 12);
        _setTokenURI(12, "bafkreifyhqywmgq3d4cmymv2v5qplrzhhdgkhqb4spnlnuiinko2i7xhaa");
        _safeMint(0xD1BEcF2CFb20861b6a9d7A6AE9ec2C69Bd1543dC, 17);
        _setTokenURI(17, "bafkreihmem36jziwdybitwybntq2zjdmddx234yqqa3hln2hwv5bxlj4ca");
        _safeMint(0x43FB19a15ae5Ee754FBef90db08e28dDd647523E, 1624);
        _setTokenURI(1624, "bafkreifhempdqqjxzbxmlwmig4idpzf7uvklkhyqs7url7sf6e27mhc7ui");
        _safeMint(0x282D99ed8BffF8ac4781916F8acA111dC1d328c2, 0);
        _setTokenURI(0, "bafkreiekdbopmevxqof3k5ukyicuxzwcpgjm4hdcqnw7xv24sgrdly3eym");
        _safeMint(0x89ad2a1f98266C760F585c56A661E7334F8f1d91, 1302);
        _setTokenURI(1302, "bafkreigxecvnxlwxv6332murdqdwranhoe4blpklbrv6zrtrs3defohd2u");
        _safeMint(0xB829Cf1B05380f62f0580bf73259F48EDA1fFc82, 903);
        _setTokenURI(903, "bafkreiczyfyva4t6kbik3tdk7dyilvryek3duphoxjvkdagnbfogzzesre");
        _safeMint(0xbFE18e498DAc767B4128Cbfe45519D4BEE0EAB52, 4900);
        _setTokenURI(4900, "bafkreieevv6jw4gqbswnmscbewepkymettz5key6fzz7qwb6rrey54snp4");
        _safeMint(0x210e299fDDD868F39243eB510cdA54414fD4e0BF, 4254);
        _setTokenURI(4254, "bafkreiecz55blbmd65sfjnxpit5sw2ozsl4j7frwd2eezh4ntsjkyngtla");
        _safeMint(0xbFE18e498DAc767B4128Cbfe45519D4BEE0EAB52, 4999);
        _setTokenURI(4999, "bafkreibdydpre5ipyr74mdeoymjxsg6uvkat3it7p4vyuots5ngshoqtne");
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