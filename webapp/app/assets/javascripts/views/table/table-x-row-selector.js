import React, { Component } from 'react'
import { connect } from 'react-redux'

const TableXRowSelector = ({ pageSize, onChange })=> {
  return (
    <div>
      <p style={{float: 'left'}}>Aantal resultaten per pagina:&nbsp;</p>
      <select value={pageSize} onChange={setSize(onChange)}>
        <option value="10">10</option>
        <option value="50">50</option>
        <option value="100">100</option>
        <option value="500">500</option>
        <option value="1000">1000</option>
        <option value="2000">2000</option>
      </select>
    </div>
  )
}
const setSize = (onChange) => (event) => {
      onChange(event.target.value)
}

export default TableXRowSelector
