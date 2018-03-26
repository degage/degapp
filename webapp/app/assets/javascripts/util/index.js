import debounce from 'lodash/debounce'

export const debounceEventHandler = (...args) => {
  const debounced = debounce(...args)
  return function(e) {
    e.persist()
    return debounced(e.target.value)
  }
}

export const checkStatus = (response) => {
  if (response.status >= 200 && response.status < 300) {
    return response
  } else {
  	console.log(response.json())
    var error = new Error('response')
    error.response = response
    throw error
  }
}