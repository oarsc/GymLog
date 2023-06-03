import './accordion.scss';
import React from 'react';
import { PropsWithChildren } from 'react';

interface AccordionProperties extends PropsWithChildren, React.HTMLProps<HTMLDivElement>  {
  title?: string,
  collapse?: boolean,
  block?: boolean,
}

interface AccordionState {
  collapsed: boolean,
  height: number
}

export default class Accordion extends React.Component<AccordionProperties, AccordionState> {  
  inflexibleRef: React.RefObject<HTMLDivElement>;
  htmlDivProps: React.HTMLProps<HTMLDivElement>;
  timeOut: NodeJS.Timeout | undefined = undefined;

  constructor(props: AccordionProperties) {
    super(props);

    const htmlDivProps: AccordionProperties = {...this.props};
    delete htmlDivProps.collapse;
    delete htmlDivProps.children;
    delete htmlDivProps.title;
    this.htmlDivProps = htmlDivProps;

    const collapsed = props.collapse ?? true;

    this.state = {
      height: collapsed? 0 : -1,
      collapsed
    };

    this.inflexibleRef = React.createRef();
  }

  handleOnClick = () => {
    if (!this.props.block) {
      this.toggle();
    }
  }

  toggle() {
    clearTimeout(this.timeOut);

    if (this.inflexibleRef.current) {
      const maxHeight = this.inflexibleRef.current.clientHeight;

      if (this.state.collapsed) {
        this.setState({ height: maxHeight, collapsed: false });
        this.timeOut = setTimeout(() => this.setState({ height: -1 }), 500);
        
      } else {
        this.setState({ height: maxHeight, collapsed: true });
        setTimeout(() => this.setState({ height: 0 }), 1);
      }
    }
  }

  override componentDidUpdate(prevProps: AccordionProperties) {
    if (prevProps.collapse != this.props.collapse && this.state.collapsed != this.props.collapse) {
      this.toggle();
    }
  }

  override render(): JSX.Element {
    const height = this.state.height < 0
      ? ''
      : `${this.state.height}px`;

    
    const classNames = ['accordioned'];
    if (this.state.collapsed) classNames.push('collapsed');
    if (this.props.className) classNames.push(this.props.className);
      
    return (
      <div {...this.htmlDivProps} className={classNames.join(' ')}>
        { this.renderTitle() }
        <div className='acc-content' style={{ height }}>
          <div
            className={`inflexible ${this.props.className ?? ''}`}
            ref={this.inflexibleRef} >
            { this.props.children }
          </div>
        </div>
      </div>
    );
  }

  renderTitle() {
    if (!this.props.title) {
      return <></>;
    }

    return <div className='accordion-header' onClick={ this.handleOnClick }>
      <span className='arrow'><span>&#x1433;</span></span>
      <span className='title'>{ this.props.title }</span>
    </div>;
  }
}