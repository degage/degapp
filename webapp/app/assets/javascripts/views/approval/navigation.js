import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Link } from 'react-router-dom'
import { selectRoute } from './../../actions/navigation'
import { Routes } from './../../reducers/navigation'

const Navigation = ({route, onNavigate}) =>
  <div>
    <ul className='nav nav-tabs'>
      <li role='presentation' className={route == Routes.CAR_APPROVAL_REQUEST ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.CAR_APPROVAL_REQUEST)}>
        <Link to='/degapp/approvals/cars/request'>In afwachting</Link>
      </li>
      <li role='presentation' className={route == Routes.CAR_APPROVAL_ACCEPTED ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.CAR_APPROVAL_ACCEPTED)}>
        <Link to='/degapp/approvals/cars/accepted'>Goedgekeurd</Link>
      </li>
      <li role='presentation' className={route == Routes.CAR_APPROVAL_REFUSED ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.CAR_APPROVAL_REFUSED)}>
        <Link to='/degapp/approvals/cars/refused'>Afgekeurd</Link>
      </li>
    </ul>
  </div>

const navigationClicked = (onNavigate, route) => () => onNavigate(route)

const mapStateToProps = (state, ownProps) => {
  return {
    route: state.navigation.view.route
}}

const mapDispatchToProps = {
  onNavigate: selectRoute
}

const NavigationContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Navigation)

export default NavigationContainer
