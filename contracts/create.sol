// SPDX-License-Identifier: MIT
pragma solidity ^0.8.2;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/draft-ERC20Permit.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Votes.sol";

contract Creativerse is ERC20, ERC20Permit, ERC20Votes {
    address admin;
    address plots;
    address gov;

    constructor() ERC20("Creativerse", "CREATE-test") ERC20Permit("Creativerse") {
        admin = msg.sender;
    }

    // Sets the plots contract
    function setPlotsContract(address p) public {
        require(msg.sender == admin);
        plots = p;
    }

    // Sets the gov contract
    function setGovContract(address g) public {
        require(msg.sender == admin);
        gov = g;
    }

    // Sets the admin. Will be set to the 0x000... address once initialized
    function setAdmin(address a) public {
        require(msg.sender == admin);
        admin = a;
    }

    function mint(uint256 amount) external {
        require(msg.sender == plots || msg.sender == gov);
        _mint(msg.sender, amount);
    }


    // The following functions are overrides required by Solidity.

    function _afterTokenTransfer(address from, address to, uint256 amount)
        internal
        override(ERC20, ERC20Votes)
    {
        super._afterTokenTransfer(from, to, amount);
    }

    function _mint(address to, uint256 amount)
        internal
        override(ERC20, ERC20Votes)
    {
        super._mint(to, amount);
    }

    function _burn(address account, uint256 amount)
        internal
        override(ERC20, ERC20Votes)
    {
        super._burn(account, amount);
    }
}