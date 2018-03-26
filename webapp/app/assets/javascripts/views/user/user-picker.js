import React, { Component } from 'react';
import Autosuggest from 'react-autosuggest';
import { connect } from 'react-redux'
import { changeUserSuggestionText, fetchUserSuggestions, clearUserSuggestions, selectSuggestionId } from './../../actions/user'

const theme = {
  container: 'autosuggest',
  input: 'form-control',
  suggestionsContainer: 'dropdown open',
  suggestionsList: 'dropdown-menu',
  suggestion: '',
  suggestionFocused: '',
};

const UserPicker = ({ suggestions, suggestion, suggestionText, onSuggestionSelected, onChangeUserSuggestionText, onSuggestionsFetchRequested, onSuggestionsClearRequested }) => {
  const inputProps = {
      placeholder: 'Geef een deel van de naam in',
      value: suggestionText,
      onChange: suggestionChanged(onChangeUserSuggestionText),
      // autoFocus: true
    }
  return (
    <div className="dropdown">
      <Autosuggest
        suggestions={suggestions}
        onSuggestionsFetchRequested={onSuggestionsFetchRequested}
        onSuggestionsClearRequested={onSuggestionsClearRequested}
        onSuggestionSelected={suggestionSelected(onSuggestionSelected)}
        getSuggestionValue={getSuggestionValue}
        renderSuggestion={renderSuggestion}
        inputProps={inputProps}
        theme={theme}
      />
    </div>)
}

const suggestionChanged = (onChangeUserSuggestionText) => (event, {newValue}) => {
  return onChangeUserSuggestionText(newValue)
}

const suggestionSelected = (onSelectUserSuggestionText) => (event, {newValue}) => {
  return onSelectUserSuggestionText(event.target.id)
}

const renderSuggestion = (suggestion) => <RenderSuggestion suggestion={suggestion} />

class RenderSuggestion extends React.Component {
  constructor(props) {
    super(props)
    this.state = {hover: false}
  }
  toggleHover(){
    this.setState({hover: !this.state.hover})
  }
  render() {
    const { suggestion } = this.props
    const linkStyle = {backgroundColor: this.state.hover ? '#f7f7f9' : ''}
    return(
      <div value={suggestion.id} style={linkStyle} onMouseEnter={this.toggleHover.bind(this)} onMouseLeave={this.toggleHover.bind(this)}>
        <div className='btn btn-link' id={suggestion.id}>
          {suggestion.lastName}{' '}{suggestion.firstName}
        </div>
      </div>
    )
  }
}

const getSuggestionValue = (suggestion) => `${suggestion.lastName} ${suggestion.firstName}`

const mapStateToProps = (state, ownProps) => {
  return{
    suggestions: state.users.suggestions,
    suggestion: state.users.view.suggestion,
    suggestionText: state.users.view.suggestionText,
    value: state.users.suggestion
}}

const mapDispatchToProps = {
  onChangeUserSuggestionText: changeUserSuggestionText,
  onSuggestionsFetchRequested: fetchUserSuggestions,
  onSuggestionsClearRequested: clearUserSuggestions,
  onSuggestionSelected: selectSuggestionId
}

const UserPickerContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(UserPicker)

export default UserPickerContainer
