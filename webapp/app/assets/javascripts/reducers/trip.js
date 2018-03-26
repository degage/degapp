import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'

export const TripsStatuses = {
  FETCHED_TRIPS: 'FETCHED_TRIPS',
  FETCHING_TRIPS: 'FETCHING_TRIPS',
  FETCHING_TRIPS_FAILED: 'FETCHING_TRIPS_FAILED',
  SET_PAGE: 'SET_PAGE',
  SET_FILTER_TRIPS: 'SET_FILTER_TRIPS'
}

export const TripStatuses = {
  INIT: 'INIT',
  FETCHED_TRIP: 'FETCHED_TRIP',
  FETCHING_TRIP: 'FETCHING_TRIP',
  FETCHING_TRIP_FAILED: 'FETCHING_TRIP_FAILED',
  CANCELING_RESERVATION: 'CANCELING_RESERVATION',
  CANCELING_RESERVATION_FAILED: 'CANCELING_RESERVATION_FAILED'
}

export const tripReducer = (state = {trips: [],
  car: {},
  trip: {
    id: null,
    driver: null,
    status: null,
    from: null,
    until: null
  },
  driver: null,
  previousDriver: null,
  nextDriver: null,
  owner: null,
  nextDate: null,
  startDate: null,
  view: {
    isTripModalOpen: false,
    trip: {
      status: TripStatuses.INIT
    },
    trips: {
      table: {
        page: 1,
        pageSize: 50,
        fullSize: 0,
        filter: '',
        showFilter: true,
        orderBy: null,
        asc: 1,
        columns: [
          {label: 'Start', sortField: null},
          {label: 'Einde', sortField: null},
          {label: 'Bestuurder', sortField: null},
          {label: 'Status', sortField: null},
          {label: 'Km standen', sortField: null}
        ]
      }
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_TRIPS':
      return update(state, {
          trips: {$set: action.trips},
          car: {$set: action.car},
          startDate: {$set: action.startDate},
          status: {$set: TripsStatuses.FETCHED_TRIPS},
        })
    case 'FETCHING_TRIPS':
      return update(state, {
          status: {$set: TripsStatuses.FETCHING_TRIPS},
        })
    case 'FETCHING_TRIPS_FAILED':
      return update(state, {
        status: {$set: TripsStatuses.FETCHING_TRIPS_FAILED},
      })
    case 'FETCHED_TRIP':
      return update(state, {
          trip: {$set: action.trip},
          owner: {$set: action.owner},
          driver: {$set: action.driver},
          previousDriver: {$set: action.previousDriver},
          nextDriver: {$set: action.nextDriver},
          nextDate: {$set: action.nextDate},
          view: {
            trip: {
              status: {$set: TripStatuses.FETCHED_TRIP}
            }
          }
        })
    case 'FETCHING_TRIP':
      return update(state, {
          view: {
            trip: {
              status: {$set: TripStatuses.FETCHING_TRIP}
            }
          }
        })
    case 'FETCHING_TRIP_FAILED':
      return update(state, {
        view: {
          trip: {
            status: {$set: TripStatuses.FETCHING_TRIP_FAILED}
          }
        }
      })
    case 'CANCELING_RESERVATION':
      return update(state, {
          view: {
            trip: {
              status: {$set: TripStatuses.CANCELING_RESERVATION}
            }
          }
        })
    case 'CANCELING_RESERVATION_FAILED':
      return update(state, {
        view: {
          trip: {
            status: {$set: TripStatuses.CANCELING_RESERVATION_FAILED}
          }
        }
      })
    case 'SORTING_TRIPS':
      return update(state, {
        status: {$set: TripsStatuses.SORTING_TRIPS}
      })
    case 'SORTED_TRIPS':
      return update(state, {
        status: {$set: TripsStatuses.SORTED_TRIPS},
        trips: {$set: action.trips},
      })
    case 'SORTING_TRIPS_FAILED':
      return update(state, {
        status: {$set: TripsStatuses.SORTING_TRIPS_FAILED},
      })
    case 'SET_PAGE':
      return update(state, {
        view: {
          trips: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER':
      return update(state, {
        view: {
          trips: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_TRIPS':
      return update(state, {
        view: {
          trips: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
    case 'SHOW_TRIP':
      return update(state, {
        view: {
          isTripModalOpen: {$set: true}
        }
      })
    case 'HIDE_TRIP':
      return update(state, {
        view: {
          isTripModalOpen: {$set: false}
        }
      })
    default:
      return state;
  }
};
