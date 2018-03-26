import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchQuarterStatistics, changeFilter, sortTable, changePage, changePagesizeQuarterStatistics } from './../actions/quarter-statistics'
import { showUser } from './../actions/user'
import { QuarterStatisticsStatuses } from './../reducers/quarter-statistics'
import Table from './table/table'
import UserModal from './user'
import Navigation from './navigation'
import QuarterStatisticsChart from './credit-management/chart/quarter-statistics-chart'

const QuarterStatistics = ({ onChangePagesize, route, quarterStatistics, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter, onChangeFilter, onSort, onChangePage, onShowUser }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onChangePagesize
  }
  return (<div>
    <Navigation route={route} />
      <div className='container'>
        <div className='row'>
          <QuarterStatisticsChart quarterStatistics={quarterStatistics} />
        </div>
        <div className='row'>
          <Table title='' rows={quarterStatistics} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
            fullSize={fullSize} filter={filter} showFilter={showFilter} columns={columns} onSort={onSort} onChangePage={onChangePage}
            onChangeFilter={onChangeFilter}
            rendererTableRow={rendererTableRow}
            rendererTableRowMethods={rendererTableRowMethods} />
        </div>
        <p></p>
        <UserModal />
      </div>
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <QuarterStatisticItem
    key={`user-stat-${row.billingId}`}
    quarterStatistic={row}
    onUserClick={rendererTableRowMethods.onShowUser}
  />

const QuarterStatisticItem = ({quarterStatistic, onUserClick}) =>
  <tr>
    <td><p>{quarterStatistic.billingDescription}</p></td>
    <td><p className="pull-right">{quarterStatistic.percentageOpen} %</p></td>
    <td><p className="pull-right">{quarterStatistic.percentagePaid} %</p></td>
    <td><p className="pull-right">{quarterStatistic.percentageOverdue} %</p></td>
    <td><p className="pull-right">€ {quarterStatistic.amountReceived.toFixed(2)}</p></td>
    <td><p className="pull-right">€ {quarterStatistic.amountToReceive.toFixed(2)}</p></td>
    <td><p className="pull-right">€ {quarterStatistic.amountPaid.toFixed(2)}</p></td>
    <td><p className="pull-right">€ {quarterStatistic.amountToPay.toFixed(2)}</p></td>
    <td><p className="text-center">{quarterStatistic.averageTimeToPay.toFixed(0)}</p></td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const mapStateToProps = (state, ownProps) => {
  return{
    quarterStatistics: state.quarterStatistics.quarterStatistics,
    fetching: state.quarterStatistics.status == QuarterStatisticsStatuses.FETCHING_PAYMENTS || state.quarterStatistics.status == QuarterStatisticsStatuses.SEARCHING_PAYMENTS,
    fetchingFailed: state.quarterStatistics.status == QuarterStatisticsStatuses.FETCHING_PAYMENTS_FAILED || state.quarterStatistics.status == QuarterStatisticsStatuses.SEARCHING_PAYMENTS_FAILED,
    page: state.quarterStatistics.view.quarterStatistics.table.page,
    pageSize: state.quarterStatistics.view.quarterStatistics.table.pageSize,
    fullSize: state.quarterStatistics.view.quarterStatistics.table.fullSize,
    filter: state.quarterStatistics.view.quarterStatistics.table.filter,
    showFilter: state.quarterStatistics.view.quarterStatistics.table.showFilter,
    columns: state.quarterStatistics.view.quarterStatistics.table.columns
}}

const mapDispatchToProps = {
  onChangeFilter: changeFilter,
  onSort: sortTable,
  onChangePage: changePage,
  onShowUser: showUser,
  onChangePagesize: changePagesizeQuarterStatistics
}

const QuarterStatisticsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(QuarterStatistics)

export default QuarterStatisticsContainer
