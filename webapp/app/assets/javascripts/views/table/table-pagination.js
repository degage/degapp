import React, { Component } from 'react'
import { sortTable } from './../../actions/invoice'
import { connect } from 'react-redux'
import isFunction from 'lodash/fp/isFunction'

const openFirstPage = (onChangePage) => (event) => {
  if (isFunction(onChangePage)) {
    onChangePage(1)
  }
}

const openLastPage = (onChangePage, lastPage) => (event) => {
  if (isFunction(onChangePage)) {
    onChangePage(lastPage)
  }
}

const openPrevPage = (onChangePage, page) => (event) => {
  if (isFunction(onChangePage)) {
    onChangePage(--page)
  }
}

const openNextPage = (onChangePage, page) => (event) => {
  if (isFunction(onChangePage)) {
    onChangePage(++page)
  }
}

const TablePagination = ({ page, pageSize, fullSize, onChangePage }) => {
  const hasNoPrevPage = page <= 1
  const hasNoNextPage = (page * pageSize) >= fullSize
  const lastPage = Math.ceil(fullSize/pageSize)
  return (
    <div>
      <div className="btn-group" role="group">
        <p style={{marginBottom: '0px', marginRight: '30px', float: 'left'}}>{`${fullSize} resultaten`}</p>
        <div style={{float: 'left'}}>
          <button type="button" className="btn btn-default" disabled={hasNoPrevPage} style={{border: 'none', padding: '0px 4px 0px 4px'}}
            onClick={openFirstPage(onChangePage)}><i className='fa fa-angle-double-left'></i></button>
          <button type="button" className="btn btn-default" disabled={hasNoPrevPage} style={{border: 'none', padding: '0px 4px 0px 4px'}}
            onClick={openPrevPage(onChangePage, page)}><i className='fa fa-angle-left'></i></button>
          <button type="button" className="btn btn-default" style={{border: 'none', padding: '0px 2px 0px 2px'}}>pagina {page} van {lastPage}</button>
          <button type="button" className="btn btn-default" disabled={hasNoNextPage} style={{border: 'none', padding: '0px 4px 0px 4px'}}
            onClick={openNextPage(onChangePage, page)}><i className='fa fa-angle-right'></i></button>
          <button type="button" className="btn btn-default" disabled={hasNoNextPage} style={{border: 'none', padding: '0px 4px 0px 4px'}}
            onClick={openLastPage(onChangePage, lastPage)}><i className='fa fa-angle-double-right'></i></button>
        </div>
      </div>
    </div>
  )
}

export default TablePagination
