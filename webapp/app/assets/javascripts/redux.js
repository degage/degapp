import {
  applyMiddleware,
  combineReducers,
  createStore,
} from 'redux';
import {persistStore, autoRehydrate} from 'redux-persist'
import {invoiceReducer as invoices} from './reducers/invoice'
import {paymentReducer as payments} from './reducers/payment'
import {userReducer as users} from './reducers/user'
import {navigationReducer as navigation} from './reducers/navigation'
import {userStatisticsReducer as userStatistics} from './reducers/user-statistics'
import {quarterStatisticsReducer as quarterStatistics} from './reducers/quarter-statistics'
import {remindersReducer as reminders} from './reducers/reminder'
import {codaReducer as codas} from './reducers/coda'
import {tripReducer as trips} from './reducers/trip'
import {carApprovalReducer as carApprovals} from './reducers/car-approval'
import {carReducer as cars} from './reducers/car'
import {snackbarReducer as snackbar} from './reducers/snackbar'
import {settingsReducer as settings} from './reducers/settings'
import {infosessionReducer as infosessions} from './reducers/infosession'

import thunk from 'redux-thunk';
import { composeWithDevTools } from 'redux-devtools-extension';

export const API_ENDPOINT = `${window.location.origin}/degapp`

export const reducers = combineReducers({
  invoices,
  payments,
  users,
  navigation,
  userStatistics,
  quarterStatistics,
  reminders,
  codas,
  trips,
  carApprovals,
  cars,
  snackbar,
  settings,
  infosessions
})

export function configureStore(initialState = {}) {
  const store = createStore(
    reducers,
    initialState,
    composeWithDevTools(
      applyMiddleware(thunk),
      // autoRehydrate()
    )
  )
  persistStore(store)
  return store
};

window.devToolsExtension ? window.devToolsExtension() : f => f

export const store = configureStore();
