import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'

export const CodasStatuses = {
  INIT: 'INIT',
  FETCHED_CODAS: 'FETCHED_CODAS',
  FETCHING_CODAS: 'FETCHING_CODAS',
  FETCHING_CODAS_FAILED: 'FETCHING_CODAS_FAILED',
  UPLOADED_CODA: 'UPLOADED_CODA',
  UPLOADING_CODA: 'UPLOADING_CODA',
  UPLOADING_CODA_FAILED: 'UPLOADING_CODA_FAILED',
  SELECTING_CODA: 'SELECTING_CODA',
  SET_PAGE_CODAS: 'SET_PAGE_CODAS',
  EDITING_STATUS: 'EDITING_STATUS',
  SET_FILTER_CODAS: 'SET_FILTER_CODAS',
  CODA_FILE_SELECTED: 'CODA_FILE_SELECTED',
  SET_PAGESIZE_CODAS: 'SET_PAGESIZE_CODAS'
}

export const CodaStatuses = {
  UPLOADING_CODA: 'UPLOADING_CODA',
  UPLOADED_CODA: 'UPLOADED_CODA',
  UPLOADING_CODA_FAILED: 'UPLOADING_CODA_FAILED',
  INACTIVE: 'INACTIVE'
}

export const codaReducer = (state = {
  codas: [],
  fileName: '',
  file: null,
  numberOfPayments: 0,
  view: {
    isCodaModalOpen: false,
    coda: {
      status: CodaStatuses.INACTIVE
    },
    codas: {
      table: {
        page: 1,
        pageSize: 10,
        fullSize: 0,
        filter: '',
        showFilter: true,
        showPagination: true,
        orderBy: null,
        asc: 1,
        columns: [
          {label: 'ID', sortField: null},
          {label: 'Bestandsnaam', sortField: null},
          {label: 'Datum', sortField: null},
          {label: 'Gebruiker', sortField: null}
        ]
      }
    }
  }
}, action) => {
  switch (action.type) {
    case 'CODAS_OPEN_MODAL':
      return update(state, {
        view: {
          isSelectCodaModalOpen: {$set: true}
        },
        codas: {$set: []},
        codaSearch: {$set: ''}
      })
    case 'CODAS_CLOSE_MODAL':
      return update(state, {
        view: {
          isSelectCodaModalOpen: {$set: false}
        }
      })
    case 'FETCHED_CODAS':
      return update(state, {
        codas: {$set: action.codas.base},
        status: {$set: CodasStatuses.FETCHED_CODAS},
        view: {
          codas: {
            table: {
              fullSize: {$set: action.codas.fullSize}
            }
          }
        },
      })
    case 'FETCHING_CODAS':
      return update(state, {
        status: {$set: CodasStatuses.FETCHING_CODAS},
      })
    case 'SELECTING_CODA':
      return update(state, {
        status: {$set: CodasStatuses.SELECTING_CODA},
        paymentId: {$set: action.paymentId},
        view: {
          isSelectCodaModalOpen: {$set: true}
        }
      })
    case 'SET_PAGE_CODAS':
      return update(state, {
        view: {
          codas: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_CODAS':
      return update(state, {
        view: {
          codas: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_CODAS':
      return update(state, {
        view: {
          codas: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
      case 'SET_PAGE_SELECT_CODA':
        return update(state, {
          view: {
            selectCoda: {
              table: {
                page: {$set: action.page}
              }
            }
          }
        })
      case 'SET_SORT_ORDER_SELECT_CODA':
        return update(state, {
          view: {
            selectCoda: {
              table: {
                orderBy: {$set: action.orderBy},
                asc: {$set: action.asc}
              }
            }
          }
        })
      case 'SET_FILTER_SELECT_CODA':
        return update(state, {
          view: {
            selectCoda: {
              table: {
                filter: {$set: action.filter}
              }
            }
          }
        })
    case 'FETCHED_CODA':
      return update(state, {
        coda: {$set: action.coda}
      })
    case 'SHOW_CODA':
      return update(state, {
        view: {
          isCodaModalOpen: {$set: true}
        }
      })
    case 'HIDE_CODA':
      return update(state, {
        view: {
          isCodaModalOpen: {$set: false}
        }
      })
    case 'HIDE_SELECT_CODA':
      return update(state, {
        view: {
          isSelectCodaModalOpen: {$set: false}
        }
      })
    case 'SET_FILTER_CODAS':
      return update(state, {
        view: {
          reminders: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
    case 'CODA_FILE_SELECTED':
      return update(state, {
        fileName: {$set: action.fileName},
        file: {$set: action.file}
      })
    case 'UPLOADING_CODA':
      return update(state, {
        view: {
          coda: {
            status: {$set: CodaStatuses.UPLOADING_CODA}
          }
        }
      })
    case 'UPLOADED_CODA':
      return update(state, {
        numberOfPayments: {$set: action.numberOfPayments},
        view: {
          coda: {
            status: {$set: CodaStatuses.UPLOADED_CODA}
          }
        }
      })
    case 'UPLOADING_CODA_FAILED':
      return update(state, {
        numberOfPayments: {$set: action.numberOfPayments},
        view: {
          coda: {
            status: {$set: CodaStatuses.UPLOADING_CODA_FAILED}
          }
        }
      })
      case 'SET_PAGESIZE_CODAS':
        return update(state, {
          view: {
            codas: {
              table: {
                pageSize: {$set: action.pageSize}
              }
            }
          }
        })
    default:
      return state;
  }
};
