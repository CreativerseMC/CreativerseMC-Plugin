// contracts/GameItem.sol
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Enumerable.sol";

contract Plot is ERC721URIStorage, ERC721Enumerable {
    constructor() ERC721("Creativerse Plot", "PLOT") {}
    event Edit(uint256 tokenId, string cid);

    function editPlot(uint256 tokenId, string memory cid) public returns (uint256) {
        if (_exists(tokenId) && ownerOf(tokenId) == msg.sender) {
            _setTokenURI(tokenId, cid);
        } else {
            _safeMint(msg.sender, tokenId);
            _setTokenURI(tokenId, cid);
        }
        emit Edit(tokenId, cid);
        
        return tokenId;
    }

    function _beforeTokenTransfer(address from, address to, uint256 tokenId)
        internal
        override(ERC721, ERC721Enumerable)
    {
        super._beforeTokenTransfer(from, to, tokenId);
    }

    function _burn(uint256 tokenId)
        internal
        override(ERC721, ERC721URIStorage)
    {
        super._burn(tokenId);
    }

    function supportsInterface(bytes4 interfaceId)
        public view
        override(ERC721, ERC721Enumerable)
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
}