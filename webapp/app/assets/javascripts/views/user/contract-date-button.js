import React, { Component } from 'react';
import Modal from 'react-modal'
// import UserPicker from './user-picker'

const ContractDateButton = ({ userId }) =>
    <button className='btn btn-default' onClick={contractButtonClicked(onCloseContractDateButton)}>
      Contract datum: {userId}
    </button>

    // onClick={contractButtonClicked(onCloseContractDateButton)}
const contractButtonClicked = (onOpenContractDateModal) => () => onOpenContractDateButton()

export default ContractDateButton
