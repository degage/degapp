import React from 'react';
import ReactDOM from 'react-dom';
import Navigation from './views/navigation'
import NavigationCars from './views/approval/navigation'
import Invoices from './views/invoices-table'
import Payments from './views/payments'
import Reminders from './views/reminders'
import UserStatistics from './views/user-statistics'
import QuarterStatistics from './views/quarter-statistics'
import CodaUpload from './views/coda-upload'
import Trips from './views/trip/trips'
import CarApprovals from './views/approval/cars'
import UnpaidInvoices from './views/credit-management/unpaid-invoices'
import ContractDateButton from './views/user/contract-date-button'
import { Routes } from './reducers/navigation'
import { Provider } from 'react-redux'
import { store } from './redux'
import { openModal, fetchInvoicesInvoices, fetchUnpaidInvoices } from './actions/invoice'
import { fetchPayment, fetchPayments } from './actions/payment'
import { fetchUserStatistics } from './actions/user-statistics'
import { fetchQuarterStatistics } from './actions/quarter-statistics'
import { fetchReminders } from './actions/reminder'
import { fetchCodas } from './actions/coda'
import { fetchTripsByCar } from './actions/trip'
import { fetchSettings } from './actions/settings'
import { selectRoute } from './actions/navigation'
import { fetchCarByUserId, changeShareCar, fetchCarInitialState, changeCarField } from './actions/car'
import { fetchUpcomingInfosessions } from './actions/infosession'
import { fetchCarApprovals } from './actions/car-approval'
import { BrowserRouter, Switch, Route, Link } from 'react-router-dom'
import Snackbar from './views/Snackbar/Snackbar'
import Registered from './views/approval/registered'
import Validating from './views/approval/validating'
import Full from './views/approval/full'
import Users from './views/user/user-list.js'
import EnrollInfosession from './views/infosession/enroll-infosession'
import CarInitialState from './views/infosession/car-initial-state'
import { fetchUsers } from './actions/user'
import Settings from './views/settings/settings'
import Reservation from './views/reservation/reservation.js'


const NavigationSwitch = ({route}) =>
  <Switch>
    <Route path='/degapp/credit/invoices' render={() => <Invoices route={Routes.INVOICES} />} />
    <Route path='/degapp/credit/payments' render={() => <Payments route={Routes.PAYMENTS} />} />
    <Route path='/degapp/credit/userstats' render={() => <UserStatistics route={Routes.USER_STATISTICS} />} />
    <Route path='/degapp/credit/quarterstats' render={() => <QuarterStatistics route={Routes.QUARTER_STATISTICS} />} />
    <Route path='/degapp/credit/reminders' render={() => <Reminders route={Routes.REMINDERS} />} />
    <Route path='/degapp/credit/coda-upload' render={() => <CodaUpload route={Routes.CODA_UPLOAD} />} />
    <Route path='/degapp/credit/users' render={() => <Users route={Routes.USERS} />}/>
    <Route path='/degapp/infosession/enroll' component={EnrollInfosession}/>
  </Switch>

const NavigationSwitchCarApprovals = ({route}) =>
  <Switch>
    <Route path='/degapp/approvals/cars/request' render={() => <CarApprovals route={Routes.CAR_APPROVAL_REQUEST} />}/>
    <Route path='/degapp/approvals/cars/accepted' render={() => <CarApprovals route={Routes.CAR_APPROVAL_ACCEPTED} />} />
    <Route path='/degapp/approvals/cars/refused' render={() => <CarApprovals route={Routes.CAR_APPROVAL_REFUSED} />} />
  </Switch>

const payments = (domElementId, route) => {
  ReactDOM.render(
    <Provider store={store}>
      <BrowserRouter >
        <div>
          <NavigationSwitch route={route}/>
          <Snackbar />
        </div>
      </BrowserRouter>
    </Provider>,
    document.getElementById(domElementId)
  );
  store.dispatch({
    type: 'SELECT_ROUTE',
    route
  })
  store.dispatch(fetchPayments())
  store.dispatch(fetchUserStatistics())
  store.dispatch(fetchInvoicesInvoices())
  store.dispatch(fetchQuarterStatistics())
  store.dispatch(fetchReminders())
  store.dispatch(fetchCodas())
  store.dispatch(fetchUsers())
  // store.dispatch(fetchTripsByCar(14))
}

const carApprovals = (domElementId, route) => {
  ReactDOM.render(
    <Provider store={store}>
      <BrowserRouter >
        <div>
          <NavigationSwitchCarApprovals route={route}/>
          <Snackbar />
        </div>
      </BrowserRouter>
    </Provider>,
    document.getElementById(domElementId)
  )
  store.dispatch(selectRoute(route))
}

const unpaidInvoices = (domElementId) => {
  ReactDOM.render(
    <Provider store={store}>
      <UnpaidInvoices />
    </Provider>,
    document.getElementById(domElementId)
  )
  store.dispatch(fetchUnpaidInvoices())
}

const settings = (domElementId) => {
  ReactDOM.render(
    <Provider store={store}>
      <Settings />
    </Provider>,
    document.getElementById(domElementId)
  )
  store.dispatch(fetchSettings())
}

const reservationFastTrack = (domElementId) => {
  ReactDOM.render(
    <Provider store={store}>
      <Reservation/>
      </Provider>,
    document.getElementById(domElementId)
  )
}
  
const contractDate = (domElementId, userId) => {
  ReactDOM.render(
    <Provider store={store}>
      <ContractDateButton userId={userId}/>
    </Provider>,
    document.getElementById(domElementId)
  )
}

const enrollInfosession = (domElementId, userId, isCarOwner) => {
  ReactDOM.render(
    <Provider store={store}>
      <BrowserRouter>
        <EnrollInfosession userId={userId} />
      </BrowserRouter>
    </Provider>,
    document.getElementById(domElementId)
  )
  // store.dispatch(fetchUpcomingInfosessions())
  store.dispatch(changeShareCar(isCarOwner))
  if (userId != null) {
    store.dispatch(fetchCarByUserId(userId))
  }
  // store.dispatch(attendingInfosessions())
}

const setInitialCarState = (domElementId, carId) => {
  ReactDOM.render(
    <Provider store={store}>
      <BrowserRouter>
        <CarInitialState carId={carId} />
      </BrowserRouter>
    </Provider>,
    document.getElementById(domElementId)
  )
  store.dispatch(changeCarField('id', carId))
  store.dispatch(fetchCarInitialState(carId))
}


window.DegageReactPort = {
  payments,
  carApprovals,
  unpaidInvoices,
  settings,
  reservationFastTrack,
  contractDate,
  enrollInfosession,
  setInitialCarState
}
