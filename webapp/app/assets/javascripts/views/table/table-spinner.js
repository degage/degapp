import React, { Component } from 'react'

const Spinner = ({ fetching, fetchingFailed, fetched, successMessage, size }) =>
  <div>{fetching ?
    <i className='fa fa-spinner fa-spin pull-right' style={{fontSize:'18px'}}></i> :
    fetchingFailed ? <div className="alert alert-danger" role="alert">Oh snap :(</div> : null}
      {fetched && successMessage != null ? <div className="alert alert-success" role="alert">{successMessage}</div> : null}
  </div>

export default Spinner
