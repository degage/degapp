import React, { Component } from 'react'
import { connect } from 'react-redux'
import { PaymentStatuses } from './../../reducers/payment'
import Spinner from './../table/table-spinner'

const PaymentCard = ({ payment, fetching, fetchingFailed }) =>
    <div className='panel panel-default col-lg-12' style={{border:'none', padding:'0'}}>
      <div className='panel-heading'>
        <b>{payment.id}</b>
        <Spinner style={{marginLeft: '20px'}} fetching={fetching} fetchingFailed={fetchingFailed} />
      </div>
      <div className='panel-body'>
        <div className='container'>
          <div className='row'>
            <div className='col-lg-9'>
              <p>{payment.number}</p>
              <p>{payment.name}</p>
              <p>{payment.date}</p>
              <p>{payment.accountNumber}</p>
              <p>{payment.address}</p>
              <p>{payment.bank}</p>
              <p>{payment.amount}{' '}€</p>
              <p>{payment.comment}{payment.structuredCommunication}</p>
              <p>{payment.status}</p>
              <p>{payment.debitType}</p>
              <p>{payment.fileName}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

const mapStateToProps = (state, ownProps) => {
  return{
    payment: state.payments.payment,
    fetching: state.payments.view.payment.status == PaymentStatuses.FETCHING_PAYMENT,
    fetchingFailed: state.payments.view.payment.status == PaymentStatuses.FETCHING_PAYMENT_FAILED
}}

const mapDispatchToProps = {
}

const PaymentCardContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(PaymentCard)

export default PaymentCardContainer
