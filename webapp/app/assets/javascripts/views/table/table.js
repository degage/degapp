import React, { Component } from 'react';
import TableHeader from './table-header'
import TablePagination from './table-pagination'
import TableSpinner from './table-spinner'
import TableXRowSelector from './table-x-row-selector'

const Table = ({ asc, orderBy, title, fetching, fetchingFailed, showFilter, showPagination, rows, page, pageSize, fullSize, columns, filter, status, onChangeFilter, onSort, onChangePage, rendererTableRow, rendererTableRowMethods }) => {
  return (
    <div className='panel panel-default col-lg-12' style={{border:'none', padding:'0', margin: '0px'}}>
      {title != null && title != '' ? <div className='panel-heading'>
        {title}
      </div> : ''}
      <div className='panel-body'>
        {showFilter ? <div>
          <div className='panel-body'>
            <input type="text" autoFocus value={filter} style={{width: '300px', marginBottom: '10px', marginRight: '10px'}}
              placeholder='Type hier je zoektermen' className="form-control" id="search-user"
              onChange={createOnChange(onChangeFilter)}/>
            <TableSpinner fetching={fetching} fetchingFailed={fetchingFailed} className="pull-right"/>
            {showPagination ? <TablePagination page={page} fullSize={fullSize} pageSize={pageSize} onChangePage={onChangePage}/> : ''}
            {showPagination ? <TableXRowSelector onChange={rendererTableRowMethods.onChangePagesize} pageSize={pageSize}/> : ''}
        </div>
        </div> : ''}
        <table className='table table-striped table-condensed no-footer table-bordered '>
          <thead>
            <TableHeader asc={asc} orderBy={orderBy} headerCols={columns} onSort={onSort} />
          </thead>
          <tbody>{rows.map(row => rendererTableRow({row, rendererTableRowMethods}))}</tbody>
        </table>
      {showPagination && fullSize > 0 ? <TablePagination page={page} fullSize={fullSize} pageSize={pageSize} onChangePage={onChangePage} /> : ''}
      {showPagination ? <TableXRowSelector onChange={rendererTableRowMethods.onChangePagesize} pageSize={pageSize}/> : ''}
      </div>
    </div>
  )
}

const sortClicked = (onSortClick, key, asc) => () => onSortClick(key, asc)

const createOnChange = (onFilterChanged) => (event) => onFilterChanged(event.target.value)

export default Table
