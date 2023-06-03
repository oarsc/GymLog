import React from 'react';
import MainAction from './main-actions';

export default class CalendarAction extends MainAction {

  override render(): JSX.Element {
    return <>
      {/*<button onClick={this.download}>Download</button>*/}
      <button onClick={this.save}>Save</button>
    </>;
  }
}
