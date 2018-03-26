import React, { Component } from 'react';


const TableHeader = ({ asc, orderBy, onSort, headerCols }) =>
  <tr key={'header-row'}>
    {headerCols.map(headerCol =>
      <th key={`th-${headerCol.label}`}>
        <TableHeaderCell asc={asc} orderBy={orderBy} onSort={onSort} columnLabel={headerCol.label} columnSortFieldName={headerCol.sortField}/>
      </th>)}
  </tr>

const TableHeaderCell = ({asc, orderBy, onSort, columnSortFieldName, columnLabel}) =>
  <table style={{width:'100%'}}>
    <tbody>
      <tr>
        <td style={{width:'5%'}}>
          {columnSortFieldName != null ? <SortIcons asc={asc} orderBy={orderBy} onSort={onSort} columnSortFieldName={columnSortFieldName} /> : null}
        </td>
        <td style={{width:'95%', height: '43px'}}>{columnLabel}</td>
      </tr>
    </tbody>
  </table>

const SortIcons = ({asc, orderBy, onSort, columnSortFieldName}) =>
    <span>
      <button onClick={sortClicked(onSort, columnSortFieldName, 1)} className='btn btn-link btn-xs' style={orderBy === columnSortFieldName && asc == '1' ? {...CurrentSortStyle} : {color: 'grey'}} ><i className='fa fa-angle-up'></i></button>
      <button onClick={sortClicked(onSort, columnSortFieldName, 0)} className='btn btn-link btn-xs' style={orderBy === columnSortFieldName && asc == '0' ? {...CurrentSortStyle} : {color: 'grey'}} ><i className='fa fa-angle-down'></i></button>
    </span>

const CurrentSortStyle = {
    color: '#000',
    WebkitTextStroke: '1px #000'
  }

const sortClicked = (onSortClick, key, asc) => () => onSortClick(key, asc)

export default TableHeader
