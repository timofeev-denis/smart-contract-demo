pragma solidity ^0.4.21;

/**
 * @title Owned
 * @dev Implementation ownership with a possibility to change owner in two actions with confirmation.
 */
contract Owned {

    address public owner;

    address public newOwner;
    
    constructor() public payable {
        owner = msg.sender;
    }

    modifier onlyOwner {
        require(owner == msg.sender);
        _;
    }

    function changeOwner(address _owner) onlyOwner public {
        require(_owner != 0);
        newOwner = _owner;
    }

    function confirmOwner() public {
        require(newOwner == msg.sender);
        owner = newOwner;
        delete newOwner;
    }
}

/**
 * @title Authorized
 * @dev Implementation authorization part of sale contract. Need to prevent not authorization sales.
 */
contract AuthorizedContract is Owned {
  	mapping (address => bool) authorized;

	function isAuthorized(address user) public view returns (bool) {
		return authorized[user];
	}
	
  	function addAddress(address _new) external onlyOwner {
  		  authorized[_new] = true;
  	}

  	function removeAddress(address _old) external onlyOwner {
  		  authorized[_old] = false;
  	}
}

/**
 * @title Authorized
 * @dev Implementation authorization part of sale contract. Need to prevent not authorization sales.
 */
contract SaleContract is AuthorizedContract {
  	
	function() external payable {
        require(authorized[msg.sender]);
    }
	
	function takeMoney() external onlyOwner {
		require(msg.sender.call.gas(3000000).value(address(this).balance)());
	}
}
