//@flow

import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchCarApprovals, sortTable, changePage, changeFilter, changePagesizeCarApprovals, openApprovalModal } from './../../actions/car-approval'
import { showUser } from './../../actions/user'
import { CarApprovalsStatuses } from './../../reducers/car-approval'
import UserModal from './../user'
import CarActionModal from './car-approval-modal'
import Navigation from './navigation'
import Table from './../table/table'
import moment from 'moment';

const badgeStyle = {
  cursor: 'pointer',
  marginRight: 6
}

const CarApprovals = ({ carApprovals, fetching, fetchingFailed, asc, orderBy, page, pageSize, fullSize, columns, filter, showPagination, showFilter,
  status, onChangeFilter, onShowUser, onOpenApprovalModal, onSort, onChangePage, onChangePagesize, route }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onOpenApprovalModal,
    onChangePagesize
  }
  return (<div>
    <Navigation route={route} />
    <Table title='' rows={carApprovals} fetching={fetching} fetchingFailed={fetchingFailed} asc={asc} orderBy={orderBy} page={page} pageSize={pageSize}
      fullSize={fullSize} filter={filter} showPagination={showPagination} showFilter={showFilter} columns={columns} onSort={onSort} onChangePage={onChangePage}
      onChangeFilter={onChangeFilter}
      rendererTableRow={rendererTableRow}
      rendererTableRowMethods={rendererTableRowMethods}/>
    <UserModal />
    <CarActionModal />
  </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) =>
  <CarApprovalItem
    key={`carApproval-${row.carApprovalId}`}
    carApprovalId={row.carApprovalId}
    submissionDate={row.submissionDate}
    car={row.car}
    user={row.user}
    admin={row.admin}
    status={row.status}
    infoSessionId={row.infoSessionId}
    enrollmentStatus={row.enrollmentStatus}
    onUserClick={rendererTableRowMethods.onShowUser}
    onOpenApprovalModalClick={rendererTableRowMethods.onOpenApprovalModal}
  />

const CarApprovalItem = ({ carApprovalId, submissionDate, car, status, user, admin, infoSessionId, enrollmentStatus, onUserClick, onOpenApprovalModalClick}) =>
  <tr>
    <td><img alt={car.imagesId} src={`/degapp/api/images/thumbnail/${car.imagesId}`} height='60'></img></td>
    <td>
      <a href={`/degapp/cars/edit?id=${car.id}`}><b>{car.name}</b></a>
      <br/>
      {car.brand}{' '}{car.type}{' - '}{car.active ? 'actief' : 'niet actief'}{' - '}{car.status}
    </td>
    <td>
      <a className='btn btn-link' style={{ paddingLeft: 0 }} onClick={userClicked(onUserClick, car.ownerUserId)}>{user.firstName}{' '}{user.lastName}</a>
      <br/>
      {car.location.street}{' '}{car.location.nu},{' '}{car.location.zip}{' '}{car.location.city}
    </td>
    <td><a href={`/degapp/infosession/view?id=${infoSessionId}`}><span className='badge'>{enrollmentStatus}</span></a></td>
    <td>{moment(submissionDate, 'DD-MM-YYYY').isAfter(moment(car.updatedAt, 'DD-MM-YYYY')) ? submissionDate : car.updatedAt}</td>
    <td>{admin == null ? '' : <a href={`/degapp/profile/byid?userId=${admin.id}`}>{admin.firstName + ' ' + admin.lastName}</a>}</td>
    <td>
      <div className="btn-group btn-group-xs">
        { admin == null
          ? <a key='ASSIGN_ADMIN' className='btn btn-xs btn-info' style={badgeStyle} onClick={assignAdminClicked(onOpenApprovalModalClick, carApprovalId)}>Admin toewijzen</a>
          : <span>
              <a key='CONTRACTSCAR' className={'btn btn-xs ' + ((car.contractFileId === 0 || car.contract === null || car.carAgreedValue === 0) ? 'btn-warning' : 'btn-success')} href={`/degapp/cars/edit?id=${car.id}#contract`} style={{ color: '#fff' }} target="_blank">Contract</a>
              <a key='CONTRACTSINSURANCE' className={'btn btn-xs ' + (car.insurance.insuranceFileId === 0 ? 'btn-warning' : 'btn-success')} href={`/degapp/cars/edit?id=${car.id}#insurance`} style={{ color: '#fff' }} target="_blank">Verzekering</a>
              <a key='CONTRACTSASSISTANCE' className={'btn btn-xs ' + (car.assistance.name === null || car.assistance.name === '' ? 'btn-warning' : 'btn-success')} href={`/degapp/cars/edit?id=${car.id}#assistance`} style={{ color: '#fff' }} target="_blank">Pechverhelping</a>
              <a key='PARKINGCARDS' className={'btn btn-xs ' + (car.parkingcard.fileId === 0 ? 'btn-warning' : 'btn-success')} href={`/degapp/cars/edit?id=${car.id}#parkingcard`} style={{ color: '#fff' }} target="_blank">Parkeerkaart</a>
              {(status === 'REQUEST')
              ? <a key='FINISH' className='btn btn-xs btn-primary' style={badgeStyle} onClick={finishClicked(onOpenApprovalModalClick, carApprovalId) }>Afronden</a>
                : (status === 'REFUSED')
                ? <a key='FINISH' className='btn btn-xs btn-primary' style={badgeStyle} onClick={finishClicked(onOpenApprovalModalClick, carApprovalId)}>Aanpassen</a>
                  : null
                }
            </span>
        }
      </div>
    </td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const finishClicked = (onOpenApprovalModalClick, carApprovalId) => () => onOpenApprovalModalClick(carApprovalId, 'FINISH')

const assignAdminClicked = (onOpenApprovalModalClick, carApprovalId) => () => onOpenApprovalModalClick(carApprovalId, 'ASSIGN_ADMIN')

const mapStateToProps = (state, ownProps) => {
  return{
    carApprovals: state.carApprovals.carApprovals,
    fetching: state.carApprovals.status == CarApprovalsStatuses.FETCHING_CAR_APPROVALS || state.carApprovals.status == CarApprovalsStatuses.SEARCHING_CAR_APPROVALS,
    fetchingFailed: state.carApprovals.status == CarApprovalsStatuses.FETCHING_CAR_APPROVALS_FAILED || state.carApprovals.status == CarApprovalsStatuses.SEARCHING_CAR_APPROVALS_FAILED,
    status: state.carApprovals.status,
    asc: state.carApprovals.view.carApprovals.table.asc,
    orderBy: state.carApprovals.view.carApprovals.table.orderBy,
    page: state.carApprovals.view.carApprovals.table.page,
    pageSize: state.carApprovals.view.carApprovals.table.pageSize,
    fullSize: state.carApprovals.view.carApprovals.table.fullSize,
    filter: state.carApprovals.view.carApprovals.table.filter,
    showFilter: state.carApprovals.view.carApprovals.table.showFilter,
    columns: state.carApprovals.view.carApprovals.table.columns,
    showPagination: state.payments.view.payments.table.showPagination,
    route: state.navigation.view.route
}}

const mapDispatchToProps = {
  fetchCarApprovals,
  onSort: sortTable,
  onChangeFilter: changeFilter,
  onChangePage: changePage,
  onChangePagesize: changePagesizeCarApprovals,
  onShowUser: showUser,
  onOpenApprovalModal: openApprovalModal
}

const CarApprovalsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(CarApprovals)

export default CarApprovalsContainer
