import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { fetchPayments, changeFilter, linkInvoice, unlinkInvoices, sortTable, changePage } from './../actions/invoice'
import { InvoicesStatuses } from './../reducers/invoice'
import TableHeader from './table/table-header'
import TablePagination from './table/table-pagination'
import { closeUserModal } from './../actions/user'

const modalStyles = {
  content : {
    top                   : '15%',
    left                  : '10%',
    right                 : '10%',
    bottom                : '15%',
    backgroundColor       : 'rgba(240, 240, 240, 1)',
    padding               : '0'
  }
};

const UserModal = ({user, isUserModalOpen, closeUserModal}) =>
  <Modal
    isOpen={isUserModalOpen}
    style={modalStyles}
    contentLabel="Modal"
    shouldCloseOnOverlayClick={true}
    onRequestClose={closeUserModal}
  >
    <button className='btn btn-primary' onClick={closeUserModal}>
      <i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten
    </button>
    <User user={user} />
  </Modal>

const creditStatusStyle = {
  display: 'inline',
  padding: '.2em .6em .3em',
  fontSize: '75%',
  fontWeight: '700',
  //lineHeight: '1',
  color: '#fff',
  textAlign: 'center',
  whiteSpace: 'nowrap',
  verticalAlign: 'baseline',
  borderRadius: '.25em',
  backgroundColor: 'rgb(217, 83, 79)'
}

const creditStatusColor = (creditStatus) =>
  creditStatus === 'REGULAR' ? {backgroundColor: '#5cb85c'} : {backgroundColor: '#d9534f'}


const User = ({ user }) =>
    <div className='panel panel-default col-lg-12 col-md-12 col-sm-12' style={{border:'none', padding:'0'}}>
      <div style={{ height: '80px'}} className='panel-heading'>
        <h2 style={{float: 'left'}}>{user.firstName}{' '}{user.lastName}</h2>
        <p style={{...creditStatusStyle, ...creditStatusColor(user.creditStatus)}}>{user.creditStatus}</p>
      </div>
      <div className='panel-body'>
        <div className='container col-lg-12 col-md-12 col-sm-12'>
          <div className='row'>
            <div className='col-lg-3 col-md-3 col-sm-4'>
              <img src={'http://www.degage.be/degapp/profile/picture?userId=' + user.id} alt={'Profielfoto '+user.id} width='200px' className='img-responsive' />
              <p><strong>Email: </strong>{user.email == null ? '(Niet gekend)' : user.email}</p>
              <p><strong>Telefoon: </strong>{user.phone == null ? '(Niet gekend)' : user.phone}</p>
              <p><strong>Gsm: </strong>{user.cellPhone == null ? '(Niet gekend)' : user.cellPhone}</p>
            </div>
            <div className='col-lg-7 col-md-7 col-sm-8' style={{border: '1px solid', borderRadius: '5px'}}>
              <h3>Gegevens</h3>
              <p><strong>Degage nummer: </strong>{user.degageId == null ? '(Niet gekend)' : user.degageId}</p>
              <p><strong>Lid sinds: </strong>{user.dateJoined == null ? '(Niet gekend)' : user.dateJoined}</p>
              <p><strong>Domicilieadres: </strong>{user.addressDomicile == null ? null : `${user.addressDomicile.street} ${user.addressDomicile.num} ${user.addressDomicile.zip} ${user.addressDomicile.city}`}</p>
              <p><strong>Verblijsadres: </strong>{user.addressResidence == null ? null : `${user.addressResidence.street} ${user.addressResidence.num} ${user.addressResidence.zip} ${user.addressResidence.city}`}</p>
              <p><strong>Rekeningnummer: </strong>{user.accountNumber == null ? '(Niet gekend)' : user.accountNumber}</p>
              <p><strong>Betalingsinfo: </strong>{user.paymentInfo}</p>
              <p><strong>Te betalen: </strong>{user.amountToPay == null ? '' : user.amountToPay.toFixed(2)}</p>
              <p><strong>Betaald: </strong>{user.amountPaid == null ? '' : user.amountPaid.toFixed(2)}</p>
              <p><strong>Balans: </strong>{user.amountPaid == null || user.amountToPay == null ? '': (user.amountToPay - user.amountPaid).toFixed(2)}</p>
              <p><strong>Ontvangt rappels: </strong><i className={`fa ${user.sendReminder ? 'fa-check-square-o' : 'fa-square-o'}`} style={{fontSize:'18px'}}></i></p>
            </div>
          </div>
        </div>
      </div>
    </div>

const mapStateToProps = (state, ownProps) => {
  return{
    user: state.users.user,
    isUserModalOpen: state.users.view.isUserModalOpen
}}

const mapDispatchToProps = {
  closeUserModal
}

const UserModalContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(UserModal)

export default UserModalContainer
