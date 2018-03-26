import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const InvoicesStatuses = {
  INIT: 'INIT',
  FETCHED_INVOICES: 'FETCHED_INVOICES',
  FETCHING_INVOICES: 'FETCHING_INVOICES',
  FETCHING_INVOICES_FAILED: 'FETCHING_INVOICES_FAILED',
  LINKING_PAYMENT_AND_INVOICE_FAILED: 'LINKING_PAYMENT_AND_INVOICE_FAILED',
  LINKING_PAYMENT_AND_INVOICE: 'LINKING_PAYMENT_AND_INVOICE',
  UNLINKED_PAYMENTS_FOR_INVOICE: 'UNLINKED_PAYMENTS_FOR_INVOICE',
  UNLINKING_PAYMENTS_FOR_INVOICE: 'UNLINKING_PAYMENTS_FOR_INVOICE',
  UNLINKING_PAYMENTS_FOR_INVOICE_FAILED: 'UNLINKING_PAYMENTS_FOR_INVOICE_FAILED',
  SEARCHING_INVOICES: 'SEARCHING_INVOICES',
  SEARCHING_INVOICES_FAILED: 'SEARCHING_INVOICES_FAILED',
  SELECTING_INVOICE: 'SELECTING_INVOICE',
  SET_PAGE: 'SET_PAGE',
  EDITING_STATUS: 'EDITING_STATUS',
  SET_PAGESIZE_INVOICES: 'SET_PAGESIZE_INVOICES',
  LINKED_PAYMENT_AND_INVOICE: 'LINKED_PAYMENT_AND_INVOICE'
}

export const InvoiceStatuses = {
  READ_ONLY: 'READ_ONLY',
  EDITING_STATUS: 'EDITING_STATUS',
  UPDATING_INVOICE_STATUS: 'UPDATING_INVOICE_STATUS',
  UPDATING_INVOICE_STATUS_FAILED: 'UPDATING_INVOICE_STATUS_FAILED'
}

export const invoiceReducer = (state = {invoices: [],
  unpaidInvoices: [],
  invoice: {
    invoice: {
      number: null,
      amount: null
    },
    user: {}
  },
  view: {
    isInvoiceModalOpen: false,
    isSelectInvoiceModalOpen: false,
    invoice: {
      status: InvoiceStatuses.READ_ONLY,
      tempStatus: ''
    },
    invoices: {
      table: {
        page: 1,
        pageSize: cookies.get('inv:ps') != null ? cookies.get('inv:ps') : 10,
        fullSize: 0,
        filter: '',
        orderBy: cookies.get('inv:orderBy') != null ? cookies.get('inv:orderBy') : 'date',
        asc: cookies.get('inv:asc') != null ? cookies.get('inv:asc') : 0,
        columns: [
          {label: 'Number', sortField: 'number'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Bedrag', sortField: 'amount'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'Betalingen', sortField: null},
          {label: 'mededeling', sortField: null}
        ],
        showFilter: true,
        showPagination: true
      }
    },
    selectInvoice: {
      table: {
        page: 1,
        pageSize: 50,
        fullSize: 0,
        filter: '',
        orderBy: 'date',
        asc: 0,
        columns: [
          {label: '', sortField: null},
          {label: 'Number', sortField: 'number'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Bedrag', sortField: 'amount'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'mededeling', sortField: null}
        ],
        showFilter: true,
        showPagination: true
      }
    },
    unpaidInvoices: {
      table: {
        page: 1,
        pageSize: 50,
        fullSize: 0,
        filter: '',
        orderBy: null,
        asc: 1,
        columns: [
          {label: 'Afrekeningsnummer', sortField: null},
          {label: 'Afrekeningsbedrag', sortField: null},
          {label: 'Afrekeningsdatum', sortField: null},
          {label: 'Mededeling', sortField: null},
        ],
        showFilter: false,
        showPagination: false
      },
      status: InvoicesStatuses.INIT
    }
  }
}, action) => {
  switch (action.type) {
    case 'INVOICES_OPEN_MODAL':
      return update(state, {
        view: {
          isSelectInvoiceModalOpen: {$set: true}
        },
        invoices: {$set: []},
        invoiceSearch: {$set: ''}
      })
    case 'INVOICES_CLOSE_MODAL':
      return update(state, {
        view: {
          isSelectInvoiceModalOpen: {$set: false}
        }
      })
    case 'FETCHED_INVOICES_INVOICES':
      return update(state, {
        invoices: {$set: action.invoices.base},
        status: {$set: InvoicesStatuses.FETCHED_INVOICES},
        view: {
          invoices: {
            table: {
              fullSize: {$set: action.invoices.fullSize}
            }
          }
        },
      })
    case 'FETCHED_INVOICES_SELECT_INVOICE':
      return update(state, {
        invoices: {$set: action.invoices.base},
        status: {$set: InvoicesStatuses.FETCHED_INVOICES},
        view: {
          selectInvoice: {
            table: {
              fullSize: {$set: action.invoices.fullSize}
            }
          }
        },
      })
    case 'FETCHING_INVOICES':
      return update(state, {
        status: {$set: InvoicesStatuses.FETCHING_INVOICES},
      })
    case 'LINKED_INVOICE_AND_PAYMENT':
      const invoices = state.invoices.map(invoiceAndUser =>
        invoiceAndUser.invoice.invoiceId === action.invoiceAndUser.invoice.invoiceId ? action.invoiceAndUser : invoiceAndUser
      )
      return update(state, {
        status: {$set: InvoicesStatuses.LINKED_INVOICE_AND_PAYMENT},
        invoices: {$set: invoices},
        view: {
          isSelectPaymentModalOpen: {$set: false}
        }
      })
    case 'LINKING_PAYMENT_AND_INVOICE_FAILED':
      return update(state, {
        status: {$set: InvoicesStatuses.LINKING_PAYMENT_AND_INVOICE_FAILED},
      })
    case 'LINKING_PAYMENT_AND_INVOICE':
      return update(state, {
        status: {$set: InvoicesStatuses.LINKING_PAYMENT_AND_INVOICE},
      })
    case 'UNLINKED_PAYMENTS_FOR_INVOICE':
      const invoices1 = state.invoices.map(invoiceAndUser =>
        invoiceAndUser.invoice.invoiceId === action.invoiceAndUser.invoice.invoiceId ? action.invoiceAndUser : invoiceAndUser
      )
      return update(state, {
        status: {$set: InvoicesStatuses.UNLINKED_PAYMENTS_FOR_INVOICE},
        invoices: {$set: invoices1}
      })
    case 'UNLINKING_PAYMENTS_FOR_INVOICE_FAILED':
      return update(state, {
        status: {$set: InvoicesStatuses.UNLINKING_PAYMENTS_FOR_INVOICE_FAILED},
      })
    case 'UNLINKING_PAYMENTS_FOR_INVOICE':
      return update(state, {
        status: {$set: InvoicesStatuses.UNLINKING_PAYMENTS_FOR_INVOICE},
      })
    case 'SELECTING_INVOICE':
      return update(state, {
        status: {$set: InvoicesStatuses.SELECTING_INVOICE},
        paymentId: {$set: action.paymentId},
        view: {
          isSelectInvoiceModalOpen: {$set: true}
        }
      })
    case 'SET_PAGE_INVOICES':
      return update(state, {
        view: {
          invoices: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_INVOICES':
      return update(state, {
        view: {
          invoices: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_INVOICES':
      return update(state, {
        view: {
          invoices: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
      case 'SET_PAGE_SELECT_INVOICE':
        return update(state, {
          view: {
            selectInvoice: {
              table: {
                page: {$set: action.page}
              }
            }
          }
        })
      case 'SET_SORT_ORDER_SELECT_INVOICE':
        return update(state, {
          view: {
            selectInvoice: {
              table: {
                orderBy: {$set: action.orderBy},
                asc: {$set: action.asc}
              }
            }
          }
        })
      case 'SET_FILTER_SELECT_INVOICE':
        return update(state, {
          view: {
            selectInvoice: {
              table: {
                filter: {$set: action.filter}
              }
            }
          }
        })
    case 'FETCHED_INVOICE':
      return update(state, {
        invoice: {$set: action.invoice}
      })
    case 'SHOW_INVOICE':
      return update(state, {
        view: {
          isInvoiceModalOpen: {$set: true}
        }
      })
    case 'HIDE_INVOICE':
      return update(state, {
        view: {
          isInvoiceModalOpen: {$set: false}
        }
      })
    case 'HIDE_SELECT_INVOICE':
      return update(state, {
        view: {
          isSelectInvoiceModalOpen: {$set: false}
        }
      })
    case 'EDITING_INVOICE_STATUS':
      return update(state, {
        view: {
          invoice: {
            status: {$set: InvoiceStatuses.EDITING_STATUS},
            tempStatus: {$set: action.tempStatus}
          }
        }
      })
    case 'CHANGE_INVOICE_STATUS':
      return update(state, {
        view: {
          invoice: {
            tempStatus: {$set: action.tempStatus}
          }
        }
      })
    case 'UPDATED_INVOICE_STATUS':
      return update(state, {
        invoice: {$set: action.invoice},
        view: {
          invoice: {
            status: {$set: InvoiceStatuses.READ_ONLY}
          }
        }
      })
    case 'UPDATING_INVOICE_STATUS':
      return update(state, {
        view: {
          invoice: {
            status: {$set: InvoiceStatuses.UPDATING_INVOICE_STATUS}
          }
        }
      })
    case 'UPDATING_INVOICE_STATUS_FAILED':
      return update(state, {
        view: {
          invoice: {
            status: {$set: InvoiceStatuses.UPDATING_INVOICE_STATUS_FAILED}
          }
        }
      })
    case 'CANCEL_UPDATE_INVOICE_STATUS':
      return update(state, {
        view: {
          invoice: {
            status: {$set: InvoiceStatuses.READ_ONLY}
          }
        }
      })
    case 'SET_PAGESIZE_INVOICES':
      return update(state, {
        view: {
          invoices: {
            table: {
              pageSize: {$set: action.pageSize}
            }
          }
        }
      })
    case 'FETCHED_UNPAID_INVOICES':
      return update(state, {
        unpaidInvoices: {$set: action.unpaidInvoices},
        view: {
          unpaidInvoices: {
            status: {$set: InvoicesStatuses.FETCHED_INVOICES},
            table: {
              fullSize: {$set: action.unpaidInvoices.length}
            }
          }
        },
      })
    case 'FETCHING_UNPAID_INVOICES':
      return update(state, {
        view: {
          unpaidInvoices: {
            status: {$set: InvoicesStatuses.FETCHING_INVOICES}
          }
        }
      })
    case 'FETCHING_UNPAID_INVOICES_FAILED':
      return update(state, {
        view: {
          unpaidInvoices: {
            status: {$set: InvoicesStatuses.FETCHING_INVOICES_FAILED}
          }
        }
      })
    case 'LINKED_PAYMENT_AND_INVOICE':
      return update(state, {
        view: {
          isSelectInvoiceModalOpen: {$set: false}
        }
      })
    default:
      return state;
  }
};
