import React, { Component } from 'react'
import { connect } from 'react-redux'
import { Link } from 'react-router-dom'
import { selectRoute } from './../actions/navigation'
import { Routes } from './../reducers/navigation'

const Navigation = ({route, onNavigate}) =>
  <div>
    <ul className='nav nav-tabs'>
      <li role='presentation' className={route == Routes.PAYMENTS ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.PAYMENTS)}>
        <Link to='/degapp/credit/payments'>Betalingen</Link>
      </li>
      <li role='presentation' className={route == Routes.INVOICES ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.INVOICES)}>
        <Link to='/degapp/credit/invoices'>Afrekeningen</Link>
      </li>
      <li role='presentation' className={route == Routes.USER_STATISTICS ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.USER_STATISTICS)}>
        <Link to='/degapp/credit/userstats'>Gebruiker stats</Link>
      </li>
      <li role='presentation' className={route == Routes.QUARTER_STATISTICS ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.QUARTER_STATISTICS)}>
        <Link to='/degapp/credit/quarterstats'>Kwartaal stats</Link>
      </li>
      <li role='presentation' className={route == Routes.REMINDERS ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.REMINDERS)}>
        <Link to='/degapp/credit/reminders'>Rappels</Link>
      </li>
      <li role='presentation' className={route == Routes.CODA_UPLOAD ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.CODA_UPLOAD)}>
        <Link to='/degapp/credit/coda-upload'>Coda opladen</Link>
      </li>
      <li role='presentation' className={route == Routes.USERS ? 'active' : null} onClick={navigationClicked(onNavigate, Routes.USERS)}>
        <Link to='/degapp/credit/users'>Users</Link>
      </li>
    </ul>
  </div>

const navigationClicked = (onNavigate, route) => () => onNavigate(route)

const mapStateToProps = (state, ownProps) => {
  return {
    route: ownProps.route != '' ? ownProps.route : state.navigation.view.route
}}

const mapDispatchToProps = {
  onNavigate: selectRoute
}

const NavigationContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Navigation)

export default NavigationContainer
