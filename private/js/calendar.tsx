import './main.scss';
import { ajax, getElementById } from './_dom/dom-utils';
import { Output } from './model/output-format';

import React from 'react';
import ReactDOM from "react-dom/client";

import BitBlock from './src/components/blocks/bit';
import CalendarAction from './src/components/calendar-actions';
import MessageManager, { AddMessageFunction } from './src/components/elements/messages';
import { forceDataReload, setData } from './src/data-manager';

interface AppProperties {
  data: Output;
};

interface AppState {
  exerciseIdFilter?: number;
  addMessage: AddMessageFunction;
};

export default class App extends React.Component<AppProperties, AppState> {
  constructor(props: AppProperties) {
    super(props);
    this.state = {
      addMessage: () => {}
    };
  }

  acquireAddMessage = (addMessage: AddMessageFunction) => {
    this.setState({ addMessage });
  }

  setGlobalExercise = (exerciseId: number) => {
    this.setState({ exerciseIdFilter: exerciseId });
  }

  override render(): JSX.Element {
    return (
      <div id='app'>
        <MessageManager acquireAddMessage={this.acquireAddMessage}/>

        <CalendarAction addMessage={this.state.addMessage} />
      
        <BitBlock />
      </div>
    );
  }
}


ajax('./read')
  .then<Output>(res => res.json())
  .then(data => {
    setData(() => data);
    forceDataReload();

    const root = ReactDOM.createRoot(getElementById('content')!);
    root.render(
        <App data={ data } />
    );
  })
