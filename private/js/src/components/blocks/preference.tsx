import './preference.scss';
import React from 'react';
import { OutputPreferences } from '../../../model/output-format';
import { getData, dataObservable } from '../../data-manager';

interface PreferenceProperties {
}

interface PreferenceState {
  prefs: OutputPreferences,
  show: boolean,
}

export default class PreferencesBlock extends React.Component<PreferenceProperties, PreferenceState> {

  limitedValues: Record<string, string[]> = {
    exercisesSortLastUsed: ['alphabetically', 'last_used']
  }

  constructor(props: PreferenceProperties) {
    super(props);

    dataObservable().subscribe(data => {
      this.setState({ prefs: data.prefs });
    });

    this.state = {
      prefs: getData().prefs,
      show: false
    }
  }

  toggle() {
    this.setState({
      show: !this.state.show
    })
  }

  override render(): JSX.Element {
    return (
      <div id='prefs-container'>
        <img src='/img/settings.svg' onClick={ this.toggle.bind(this) }/>

        {this.state.show && (
          <table id='prefs-table'>
          <thead>
            <tr>
              <th>Key</th>
              <th>Value</th>
              <th>Type</th>
            </tr>
          </thead>
          <tbody>
            {
              Object.entries(this.state.prefs).map(([key, value]) => 
                <tr key={key}>
                  <td>{ key }</td>
                  <td>{ this.renderField(key, value) }</td>
                  <td>{ typeof value }</td>
                </tr>
              )
            }
          </tbody>
        </table>
      )}
      </div>
    );
  }

  renderField(key: string, value: string | number | boolean): JSX.Element {

    if (key in this.limitedValues) {
      return (
        <select>
          {
            this.limitedValues[key]
              .map(optVal => <option key={optVal} value={ optVal }>{ optVal }</option>)
          }
        </select>
      )
    } else if (typeof value == 'string') {
      return <input type='text' value={ value } readOnly />;

    } else if (typeof value == 'number') {
      return <input type='text' value={ value } readOnly />;

    } else if (typeof value == 'boolean') {
      return <input type='checkbox' checked={value} readOnly />;
    }
    return <></>;
  }
}
