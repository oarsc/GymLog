import './gym.scss';
import React from 'react';
import { dataObservable, getData } from '../../data-manager';

interface GymProperties {
}

interface GymState {
  gyms: string[],
  show: boolean,
}

export default class GymBlock extends React.Component<GymProperties, GymState> {

  constructor(props: GymProperties) {
    super(props);

    dataObservable().subscribe(data => {
      this.setState({ gyms: data.gyms });
    });

    this.state = {
      gyms: getData().gyms,
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
      <div id='gyms-container'>
        <img src='/img/building.svg' onClick={ this.toggle.bind(this) }/>

        {this.state.show && (
          <table id='gyms-table'>
          <thead>
            <tr>
              <th>Id</th>
              <th>Name</th>
            </tr>
          </thead>
          <tbody>
            {
              this.state.gyms.map((value, idx) => 
                <tr key={idx}>
                  <td>{ idx+1 }</td>
                  <td>{ value }</td>
                </tr>
              )
            }
          </tbody>
        </table>
      )}
      </div>
    );
  }
}
