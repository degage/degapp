import React, { Component } from 'react'
import Modal from 'react-modal'
import { connect } from 'react-redux'
import { changeApprovalField, acceptCar, refuseCar, closeApprovalModal, assignAdmin, changeSelectedAdmin } from './../../actions/car-approval'
import DatePicker from 'react-datepicker'
import moment from 'moment'
import './../../../stylesheets/react-datepicker.css'

const modalStyles = {
  content: {
    top: '15%',
    left: '10%',
    right: '10%',
    bottom: '15%',
    backgroundColor: 'rgba(220, 220, 220, 1)',
    padding: '0'
  }
}

const CarActionModal = ({ isApproveModalOpen, approvalModalActionType, closeApprovalModal, carApproval, car, onChangeField, onAcceptCar, onRefuseCar, onAssignAdmin, carAdmins, selectedCarAdmin, onChangeCarAdmin }) => {
  return <Modal
    isOpen={isApproveModalOpen}
    style={modalStyles}
    contentLabel="Modal"
    shouldCloseOnOverlayClick={true}
    onRequestClose={closeApprovalModal}
  >
    { (car != null)
      ? (approvalModalActionType === 'FINISH')
        ? <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', margin: 20, flexWrap: 'wrap' }}>
              <h2>Goedkeuringsaanvraag auto</h2>
              <button className='btn btn-primary' style={{ height: 30 }} onClick={closeApprovalModal}>
                <i className='fa fa-close' style={{ marginRight: '10px' }}></i>Sluiten
              </button>
            </div>
            {approveCarForm(carApproval, car, onChangeField, onAcceptCar, onRefuseCar) }
          </div>
        : (approvalModalActionType === 'ASSIGN_ADMIN')
          ? <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', margin: 20, flexWrap: 'wrap' }}>
              <h2>Admin toewijzen</h2>
              <button className='btn btn-primary' style={{ height: 30 }} onClick={closeApprovalModal}>
                <i className='fa fa-close' style={{ marginRight: '10px' }}></i>Sluiten
              </button>
            </div>
            {assignAdminForm(carApproval, car, onAssignAdmin, carAdmins, selectedCarAdmin, onChangeCarAdmin)}
          </div>
          : null
      : null
    }
  </Modal>
}

const approveCarForm = (carApproval, car, onChangeField, onAcceptCar, onRefuseCar) =>
  <div>
    {(car.contractFileId === 0 || car.contract === null || car.carAgreedValue === 0 || car.insurance.insuranceFileId === 0 || car.assistance.fileId === 0 || car.parkingcard.fileId === 0)
      ?<div className='panel panel-danger' style={{ height: '100%', margin: 20 }}>
        <div className='panel-heading' style={{ height: 56, lineHeight: '28px' }}>
          Opgelet!
        </div>
        <div className='panel-body'>
          <ul>
            {(car.contractFileId === 0 || car.contract === null || car.carAgreedValue === 0) ? <li>Er ontbreken contractgegevens.</li>: null}
            {(car.insurance.insuranceFileId === 0) ? <li>Het verzekeringscontract ontbreekt.</li> : null}
            {(car.assistance.name === 0) ? <li>Er ontbreekt info over het pechverhelpingscontract.</li> : null}
            {(car.parkingcard.fileId === 0) ? <li>De parkeerkaart ontbreekt.</li> : null}
          </ul>
        </div>
      </div>
      : null
    }
    <div className='panel panel-default' style={{ height: '100%', margin: 20 }}>
      <div className='panel-heading' style={{ height: 56, lineHeight: '28px' }}>
        <i className='fa fa-car'></i> Auto
      </div>
      <div className='panel-body'>
          <dl className="dl-horizontal">
            <dt>Naam</dt>
            <dd>{car.name}</dd>
            <dt>Merk</dt>
            <dd>{car.brand}</dd>
            <dt>Type</dt>
            <dd>{car.type}</dd>
          </dl>
      </div>
    </div>
    <div className='panel panel-default' style={{ height: '100%', margin: 20 }}>
      <div className='panel-heading' style={{ height: 56, lineHeight: '28px' }}>
        <i className='fa fa-pencil-square-o'></i> Aanvraag
      </div>
      <div className='panel-body'>
        <div className='form'>
          <div className={`form-group`}>
            <label htmlFor='message'>Opmerkingen</label>
            <textarea id="message" rows="4" className="form-control input-md" value={carApproval.adminMessage || ''} onChange={fieldChanged(onChangeField, 'adminMessage', carApproval.id)}></textarea>
            <button className='btn btn-xs btn-success' onClick={onAcceptCarClicked(onAcceptCar, carApproval.carApprovalId)}>Aanvaarden</button>
            <button className='btn btn-xs btn-danger' onClick={onRefuseCarClicked(onRefuseCar, carApproval.carApprovalId)}>Verwerpen</button>
          </div>
        </div>
      </div>
    </div>
  </div>


const assignAdminForm = (carApproval, car, onAssignAdmin, carAdmins, selectedCarAdmin, onChangeCarAdmin) =>
  <div>
    <div className='panel panel-default' style={{ height: '100%', margin: 20 }}>
      <div className='panel-heading' style={{ height: 56, lineHeight: '28px' }}>
        <i className='fa fa-car'></i> Auto
      </div>
      <div className='panel-body'>
        <dl className="dl-horizontal">
          <dt>Naam</dt>
          <dd>{car.name}</dd>
          <dt>Merk</dt>
          <dd>{car.brand}</dd>
          <dt>Type</dt>
          <dd>{car.type}</dd>
        </dl>
      </div>
    </div>
    <div className='panel panel-default' style={{ height: '100%', margin: 20 }}>
      <div className='panel-heading' style={{ height: 56, lineHeight: '28px' }}>
        <i className='fa fa-pencil-square-o'></i> Admin toewijzen
      </div>
      <div className='panel-body'>
        <div className='form'>
          <div className={`form-group`}>
            <select value={selectedCarAdmin == null ? undefined : selectedCarAdmin} onChange={onChangeCarAdminClicked(onChangeCarAdmin)} >
              {carAdmins.map((carAdmin) => <option key={`car-admin-${carAdmin.id}`} value={carAdmin.id}>{carAdmin.firstName + ' ' + carAdmin.lastName}</option>)}
            </select>

            <button className='btn btn-xs btn-success' onClick={onAssignAdminClicked(onAssignAdmin, carApproval.carApprovalId)}>Toewijzen</button>
          </div>
        </div>
      </div>
    </div>
  </div>

const onAcceptCarClicked = (onAcceptCar, carApprovalId) => () => onAcceptCar(carApprovalId)

const onRefuseCarClicked = (onRefuseCar, carApprovalId) => () => onRefuseCar(carApprovalId)

const fieldChanged = (onChangeField, fieldName, carApprovalId) => (event) => onChangeField(carApprovalId, fieldName, event.target.value)

const onChangeCarAdminClicked = (onChangeCarAdmin) => (UIEvent) => onChangeCarAdmin(UIEvent.target.value)

const onAssignAdminClicked = (onAssignAdmin, carApprovalId) => () => onAssignAdmin(carApprovalId)

const mapStateToProps = (state, ownProps) => {
  const { isApproveModalOpen, carApprovalId, approvalModalActionType } = state.carApprovals.view
  let car, carApproval

  if (isApproveModalOpen) {
    carApproval = state.carApprovals.carApprovals.find(carApproval => carApproval.carApprovalId === carApprovalId)
    car = carApproval.car
  }
  return {
    isApproveModalOpen,
    approvalModalActionType,
    carApproval,
    car,
    carAdmins: state.carApprovals.carAdmins,
    selectedCarAdmin: state.carApprovals.selectedCarAdmin
  }
}

const mapDispatchToProps = {
  closeApprovalModal,
  onChangeField: changeApprovalField,
  onAcceptCar: acceptCar,
  onRefuseCar: refuseCar,
  onAssignAdmin: assignAdmin,
  onChangeCarAdmin: changeSelectedAdmin
}

const CarActionModalContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(CarActionModal)

export default CarActionModalContainer
