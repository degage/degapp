import React, { Component } from 'react';
import { connect } from 'react-redux';
import Autosuggest from 'react-autosuggest';
import { changeCarSuggestionText, fetchCarSuggestions, clearCarSuggestions, selectSuggestionId } from './../../actions/car'
import './../../../stylesheets/react-datepicker.css'
import './../../../stylesheets/scroll-for-autosuggest.css'

const theme = {
    container: 'autosuggest',
    input: 'form-control',
    suggestionsContainer: 'dropdown open',
    suggestionsList: 'dropdown-menu',
    suggestion: '',
    suggestionFocused: '',
};

const infoStyle = {
    display: 'inline',
    padding: '.2em .6em .3em',
    fontSize: '75%',
    fontWeight: '700',
    //lineHeight: '1',
    color: '#fff',
    textAlign: 'center',
    whiteSpace: 'nowrap',
    verticalAlign: 'baseline',
    borderRadius: '.25em',
}

const color = color => {return {backgroundColor: color}}
const fuelColor = type => {
    switch (type.toLowerCase()){
        case 'hybrid':
            return {backgroundColor: "green"}
        case 'diesel':
            return {backgroundColor: "#646464"}
        case 'petrol':
            return {backgroundColor: "orange"}
        case 'cng':
            return {backgroundColor: "olivedrab"}
        case 'lpg':
            return {backgroundColor: "darkolivegreen"}
        case 'electric':
            return {backgroundColor: "darkgreen"}
    }
}

const ReservationFastTrack = ({ suggestions, suggestion, suggestionText, onSuggestionSelected, onChangeCarSuggestionText, onSuggestionsFetchRequested, onSuggestionsClearRequested }) => {
    const inputProps = {
        placeholder: 'Welke auto wilt u reserveren?',
        value: suggestionText,
        onChange: suggestionChanged(onChangeCarSuggestionText),
        onFocus: select(),
        autoFocus: true
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

const select = () => (event) => {
  event.target.select()
}

const suggestionChanged = (onChangeCarSuggestionText) => (event, {newValue}) => {
    return onChangeCarSuggestionText(newValue)
}

const suggestionSelected = (onSuggestionSelected) => (event, {newValue}) => {
  console.log(event.target.id)
    return onSuggestionSelected(event.target.id)
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
            <div value={suggestion.id} id={suggestion.auto.id} style={linkStyle} onMouseEnter={this.toggleHover.bind(this)} onMouseLeave={this.toggleHover.bind(this)}>

                <div className='btn btn-link' style={{paddingBottom: '0px'}} id={suggestion.auto.id}>
                    {suggestion.auto.name}
                </div>
                <div className="pull-right">
                    <p style={{...infoStyle, ...color("darkgoldenrod")}} id={suggestion.auto.id}>{suggestion.auto.seats} zetels</p>
                    <p style={{...infoStyle, ...color("darkgoldenrod")}} id={suggestion.auto.id}>{suggestion.auto.doors} deurs</p>
                    <p style={{...infoStyle, ...color("grey")}} id={suggestion.auto.id}>{suggestion.auto.year}</p>
                    <p style={{...infoStyle, ...fuelColor(suggestion.auto.fuel)}} id={suggestion.auto.id}>{suggestion.auto.fuel == "PETROL" ? "BENZINE" : suggestion.auto.fuel}</p>
                    { suggestion.auto.manual ?
                        <p style={{...infoStyle, ...color("#8888cc")}} id={suggestion.auto.id}>Automatisch</p> : ''
                    }
                    { suggestion.auto.gps ?
                        <p style={{...infoStyle, ...color("#88cc88")}} id={suggestion.auto.id}>Gps</p> : ''
                    }
                    { suggestion.auto.hook ?
                        <p style={{...infoStyle, ...color("#aaaaaa")}} id={suggestion.auto.id}>Trekhaak</p> : ''
                    }

                </div>

                <div>
                    <p id={suggestion.auto.id} style={{paddingLeft: '12px'}}>{suggestion.auto.brand} {suggestion.auto.type} {suggestion.auto.location.street} {suggestion.auto.location.num}, {suggestion.auto.location.city}</p>
                </div>
            </div>
        )
    }
}

const getSuggestionValue = (suggestion) => `${suggestion.auto.name}`

const mapStateToProps = (state) => {
  return{
      suggestions: state.cars.suggestions,
      suggestion: state.cars.suggestion,
      suggestionText: state.cars.view.cars.table.filter,
      value: state.cars.suggestion
}}

const mapDispatchToProps = {
    onChangeCarSuggestionText: changeCarSuggestionText,
    onSuggestionsFetchRequested: fetchCarSuggestions,
    onSuggestionsClearRequested: clearCarSuggestions,
    onSuggestionSelected: selectSuggestionId
}

const ReservationFastTrackContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(ReservationFastTrack)

export default ReservationFastTrackContainer
