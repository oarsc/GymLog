import './main.scss';
import { ajax, getElementById } from './_dom/dom-utils';
import { Output } from './model/output-format';

import React from 'react';
import ReactDOM from "react-dom/client";

import PreferenceBlock from './src/components/blocks/preference';
import GymBlock from './src/components/blocks/gym';
import ExerciseBlock from './src/components/blocks/exercise';
import BitExerciseBlock from './src/components/blocks/bit-exercise';
import MainAction from './src/components/main-actions';
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

        <PreferenceBlock/>
        <GymBlock/>

        <MainAction addMessage={this.state.addMessage} />
        
        <ExerciseBlock
          addMessage={this.state.addMessage}
          onGlobalExercise={this.setGlobalExercise} />

        <BitExerciseBlock
          exerciseId={this.state.exerciseIdFilter} />

        <div id='footer-page-space' />
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
