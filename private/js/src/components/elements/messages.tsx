import './messages.scss';
import React from 'react';

export type AddMessageFunction = (text: string, time?: number) => void;

interface MessageManagerProperties {
  acquireAddMessage: (addMessage: AddMessageFunction) => void
}

interface MessageManagerState {
  activeMessages: JSX.Element[]
}

export default class MessageManager extends React.Component<MessageManagerProperties, MessageManagerState> {  
  constructor(props: MessageManagerProperties) {
    super(props);

    this.state = {
      activeMessages: []
    };

    props.acquireAddMessage(this.addMessage.bind(this));
  }

  override render(): JSX.Element {
    return (
      <div id='notification-area'>
        { this.state.activeMessages }
      </div>
    );
  }

  addMessage(text: string, time: number = 2000) {
    const message = <div key={Math.random()} className='notification'>{ text }</div>;
    this.setState({ activeMessages: [...this.state.activeMessages, message] });

    setTimeout(() => {
      const idx = this.state.activeMessages.indexOf(message);
      
      const messages = [ ...this.state.activeMessages ];
      messages.splice(idx, 1);

      this.setState({ activeMessages: messages });
    }, time);
  }
}