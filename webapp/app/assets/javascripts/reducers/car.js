import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import {
  CHANGE_SHARE_CAR, CHANGE_CAR_FIELD, CREATING_CAR_FAILED, CREATING_USER_ROLE_FAILED, CAR_IMAGE_FILE_SELECTED,
  UPLOADED_CAR_IMAGE, UPLOADING_CAR_IMAGE, UPLOADING_CAR_IMAGE_FAILED, SET_CAR_VALIDATION, CHANGE_ADDRESS_FIELD, CHANGE_INSURANCE_FIELD
} from '../actions/car'

import moment from 'moment';


export const CarStatuses = {
  INIT: 'INIT',
  FETCHED_CAR: 'FETCHED_CAR',
  CREATED_CAR: 'CREATED_CAR',
  CREATING_CAR: 'CREATING_CAR',
  CREATING_CAR_FAILED: 'CREATING_CAR_FAILED',
  CREATING_USER_ROLE_FAILED: 'CREATING_USER_ROLE_FAILED',
  CAR_VALIDATION_FAILED: 'CAR_VALIDATION_FAILED',
  SET_CAR_SUGGESTION_TEXT:'SET_CAR_SUGGESTION_TEXT',
  FETCHED_CARS: 'FETCHED_CARS',
  FETCHING_CAR_SUGGESTIONS: 'FETCHING_CAR_SUGGESTIONS',
  FETCHING_CAR_SUGGESTIONS_FAILED: 'FETCHING_CAR_SUGGESTIONS_FAILED',
  FETCHED_INITIAL_STATE_CAR: 'FETCHED_INITIAL_STATE_CAR',
  FETCHING_CAR_INITIAL_STATE_FAILED: 'FETCHING_CAR_INITIAL_STATE_FAILED',
  SET_CAR_SUGGESTION_ID: 'SET_CAR_SUGGESTION_ID',
  SET_CAR_SUGGESTION: 'SET_CAR_SUGGESTION',
  FETCHED_CAR_SUGGESTIONS: 'FETCHED_CAR_SUGGESTIONS',
  SET_CAR_START_DATEPICKER: 'SET_CAR_START_DATEPICKER',
  SET_CAR_STOP_DATEPICKER: 'SET_CAR_STOP_DATEPICKER',
  SWITCH_EXTRA_INFO: 'SWITCH_EXTRA_INFO',
  EXTRA_INFO_CHANGE: 'EXTRA_INFO_CHANGE',
  CREATING_RESERVATION: 'CREATING_RESERVATION',
  CREATING_RESERVATION_FAILED: 'CREATING_RESERVATION_FAILED',
  CREATED_RESERVATION: 'CREATED_RESERVATION',
  SWITCH_RESERVE_BTN: 'SWITCH_RESERVE_BTN',
  END_DATE_BEFORE_START_DATE: 'END_DATE_BEFORE_START_DATE',
  START_DATE_AFTER_END_DATE: 'START_DATE_AFTER_END_DATE'
}

export const CarValidationStatuses = {
  INVALID: 'INVALID',
  VALID: 'VALID',
  UNKNOWN: 'UNKNOWN',
}

export const carReducer = (state = {
  shareCar: false,
  car: {
    id: -1,
    name: 'temporary',
    email: 'temporary@degage.be',
    brand: '',
    fuel: 'ELECTRIC',
    type: '',
    manual: false,
    locationId: '',
    street: '',
    number: '',
    zip: '',
    city: '',
    seats: 0,
    doors: 0,
    year: '',
    fuelEconomy: 0,
    estimatedValue: 0,
    ownerAnnualKm: 0,
    comments: '',
    imagesId: '',
    insurance: {
      insuranceNameBefore: '',
      expiration: moment()
    },
    location: {
      city: '',
      street: '',
      num: '',
      zip: ''
    },
    status: 'REGISTERED'
  },
  carImageFileName: null,
  carInitialFiles: null,
  carInitialFileName: null,
  userId: null,
  reservation: {
    datepicker: {
        startDate: null,
        endDate: null
    },
      extraInfoState: false,
      extraInfoText:''
  },
  suggestions: [],
  view: {
    err:'',
    isDisabled: true,
    suggestions: {
        table: {
            fullSize: 0
        }
    },
    suggestionText:'',
    car: {
      status: CarStatuses.INIT,
      validationStatus: CarValidationStatuses.UNKNOWN
    },
    cars: {
      table: {
        page: 1,
        pageSize: 50,
        fullSize: 0,
        filter: '',
        showFilter: true,
        orderBy: null,
        asc: 1,
        columns: [
          {label: 'Nummer', sortField: 'number'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Beschrijving', sortField: 'description'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'Afrekening', sortField: 'invoice'}
        ]
      }
    }
  }
}, action) => {
  switch (action.type) {
    case CHANGE_SHARE_CAR:
      return update(state, {
        shareCar: { $set: action.shareCar }
      })
    case CHANGE_CAR_FIELD:
      return update(state, {
        car: {
          [action.fieldName]: { $set: action.fieldValue }
        }
      })
    case CHANGE_ADDRESS_FIELD:
      return update(state, {
        car: {
          location: {
            [action.fieldName]: { $set: action.fieldValue }
          }
        }
      })
    case CHANGE_INSURANCE_FIELD:
      return update(state, {
        car: {
          insurance: {
            [action.fieldName]: { $set: action.fieldValue }
          }
        }
      })
    case 'CREATED_CAR':
      return update(state, {
          car: {$set: action.car},
          view: {
            car: {
              status: {$set: CarStatuses.CREATED_CAR},
            }
          },
          status: {$set: CarStatuses.CREATED_CAR},
        })
    case 'UPDATE_CAR_PROPERTY':
      return update(state, {
          car: {
            [action.fieldName]: {$set: action.value}
          }
        })
    case 'CREATING_CAR':
      return update(state, {
        view: {
          car: {
            status: {$set: CarStatuses.CREATING_CAR},
          }
        }
      })
    case 'CREATING_CAR_FAILED':
      return update(state, {
        view: {
          car: {
            status: {$set: CarStatuses.CREATING_CAR_FAILED},
          }
        }
      })
    case 'FETCHED_CAR':
      return update(state, {
        car: { $set: action.car },
        view: {
          car: {
            status: { $set: CarStatuses.FETCHED_CAR }
          }
        }
      })
    case 'SET_USER':
      return update(state, {
        userId: { $set: action.userId }
      })
    case 'FETCHING_CAR':
      return update(state, {
          status: {$set: CarStatuses.FETCHING_CAR},
        })
    case 'FETCHING_CAR_INITIAL_STATE_FAILED':
      return update(state, {
        view: {
          car: {
            status: { $set: CarStatuses.FETCHING_CAR_INITIAL_STATE_FAILED }
          }
        },
        status: { $set: CarStatuses.FETCHING_CAR_INITIAL_STATE_FAILED},
      })
    case 'FETCHED_INITIAL_STATE_CAR':
      return update(state, {
        carInitialFiles: { $set: action.carInitialFiles },
        view: {
          car: {
            status: { $set: CarStatuses.FETCHED_INITIAL_STATE_CAR }
          }
        }
      })
    case 'CAR_INITIAL_STATE_FILE_SELECTED':
      return update(state, {
        carInitialFileName: { $set: action.fileName },
        carInitialFile: { $set: action.file }
      })

    case 'FETCHING_CAR_FAILED':
      return update(state, {
        view: {
          car: {
            status: { $set: CarStatuses.FETCHING_CAR_FAILED }
          }
        },
        status: { $set: CarStatuses.FETCHING_CAR_FAILED },
      })
    case 'SET_PAGE':
      return update(state, {
        view: {
          car: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER':
      return update(state, {
        view: {
          car: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_CAR':
      return update(state, {
        view: {
          car: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
    case CREATING_USER_ROLE_FAILED:
      return update(state, {
        view: {
          car: {
            status: { $set: CarStatuses.CREATING_USER_ROLE_FAILED }
          }
        }
      })
    case CREATING_CAR_FAILED:
      return update(state, {
        view: {
          car: {
            status: { $set: CarStatuses.CREATING_CAR_FAILED }
          }
        }
      })
    case 'CAR_IMAGE_FILE_SELECTED':
      return update(state, {
        carImageFileName: { $set: action.fileName },
        carImageFile: { $set: action.file }
      })
    case UPLOADED_CAR_IMAGE:
      return update(state, {
        car: {
          imagesId: { $set: action.carImage.fileId }
        }
      })
    case SET_CAR_VALIDATION:
      return update(state, {
        view: {
          car: {
            validationStatus: { $set: action.valid ? CarValidationStatuses.VALID : CarValidationStatuses.INVALID }
          }
        }
      })
      case 'SET_CAR_SUGGESTION_TEXT':
          return update(state, {
              view: {
                  cars:{
                      table:{
                          filter: {$set: action.suggestionText}
                      }
                  }
              }
          })
      case 'FETCHED_CAR_SUGGESTIONS':
          return update(state, {
              suggestions: {$set: action.cars},
              view: {
                  suggestions: {
                      table: {
                          fullSize: {$set: action.fullSize}
                      }
                  }
              },
              status: {$set: CarStatuses.FETCHED_CARS},
          })
      case 'FETCHING_CAR_SUGGESTIONS':
          return update(state, {
              status: {$set: CarStatuses.FETCHING_CAR_SUGGESTIONS}
          })
      case 'FETCHING_CAR_SUGGESTIONS_FAILED':
          return update(state, {
              status: {$set: CarStatuses.FETCHING_CAR_SUGGESTIONS_FAILED}
          })
      case 'SET_CAR_SUGGESTION_ID':
          const suggestion = state.suggestions.find(sugg => sugg.auto.id == action.carId)
          return update(state, {
              view: {
                  hasSuggestionChanged: {$set: true},
                  suggestion: {$set: suggestion}
              }
          })
      case 'SET_CAR_SUGGESTION':
          return update(state, {
              view: {
                  suggestion: {$set: action.car},
                  hasSuggestionChanged: {$set: state.view.suggestion != null && state.view.suggestion.auto.id != null && action.car != null && action.car.id != state.view.suggestion.auto.id}
              }
          })
      case 'SET_CAR_START_DATEPICKER':
          return update(state, {
              reservation:{
                  datepicker: {
                    startDate: {$set: action.datepicker }
                  }
              },
              view: {
                  err: {$set: action.err }
              } 
          })
      case 'SET_CAR_STOP_DATEPICKER':
          return update(state, {
              reservation: {
                  datepicker: {
                      endDate: {$set: action.datepicker}
                  }
              },
              view: {
                  err: {$set: action.err }
              }
          })
      case 'SWITCH_EXTRA_INFO':
          return update(state, {
              reservation: {
                  extraInfoState: {$set: action.newState}
              }
          })
      case 'EXTRA_INFO_CHANGE':
          return update(state, {
              reservation: {
                  extraInfoText: {$set: action.leText}
              }
          })
      case 'CREATING_RESERVATION_FAILED':
        return update(state, {
          status: {$set: CarStatuses.CREATING_RESERVATION_FAILED},
          view:{
            err: {$set: action.err}
          }
        })  
      case 'CREATING_RESERVATION':
        return update(state, {
          status: {$set: CarStatuses.CREATING_RESERVATION}
        })
      case 'CREATED_RESERVATION':
        return update(state, {
          status: {$set: CarStatuses.CREATED_RESERVATION}
        })
      case 'SWITCH_RESERVE_BTN':
        return update(state, {
          view: {
            isDisabled: {$set: action.isDisabled}
          }
        })
      case 'END_DATE_BEFORE_START_DATE':
        return update(state, {
          reservation: {
              datepicker: {
                  endDate: {$set: action.datepicker}
              }
          },
          view: {
            err: {$set: action.err }
          }
        })
      case 'START_DATE_AFTER_END_DATE':
      return update(state, {
        reservation:{
            datepicker: {
              startDate: {$set: action.datepicker }
            }
        },
        view: {
          err: {$set: action.err }
        }
      })
    default:
      return state;
  }
};
