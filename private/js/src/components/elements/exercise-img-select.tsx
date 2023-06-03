import './exercise-img-select.scss';
import { ajax } from '../../../_dom/dom-utils';
import React, { SyntheticEvent } from 'react';

interface ImageSelectorProperties  {
  value: string,
  onChange: (image: string) => void
}

interface ImageSelectorState {
  panelVisible: boolean;
}

let images: string[] = [];
ajax('/read/images')
  .then(res => res.json())
  .then(res => images = res);

export default class ExerciseImageSelect extends React.Component<ImageSelectorProperties, ImageSelectorState> {

  static blackListImage: string[] = [];

  constructor(props: ImageSelectorProperties) {
    super(props);

    this.state = {
      panelVisible: false
    }
  }

  buildImage = (value: string) => `/img/previews/${value}.png`;

  temp: any;

  togglePanel = () => {
    const value = !this.state.panelVisible;
    this.setState({ panelVisible: value });

    if (value)
      window.addEventListener('mousedown', this.globalFunction);
    else
      window.removeEventListener('mousedown', this.globalFunction);
  };

  globalFunction = (ev: MouseEvent) => {
    const target = ev.target as HTMLElement;
    if (!target.closest('.image-selector')) {
      this.togglePanel();
    }
  }

  filterImage = (image: string) => {
    return (ev: SyntheticEvent<HTMLImageElement>) => {
      const element = ev.target as HTMLImageElement;
      if (element.width !== 100) {
        ExerciseImageSelect.blackListImage.push(image);
        element.style.display = 'none';
      } else {
        element.style.width = '100px';
      }
    }
  }

  filterOut(src: string) {
    return ExerciseImageSelect.blackListImage.indexOf(src) < 0;
  }

  select(image: string) {
    const { onChange } = this.props;
    const togglePanel = this.togglePanel.bind(this);

    return function() {
      if (onChange) onChange(image);
      togglePanel();
    }
  }

  override render(): JSX.Element {
    return (
      <div className='image-selector'>
        <div className='selected' onClick={this.togglePanel}>
          <img src={ this.buildImage(this.props.value) } />
        </div>
        { this.state.panelVisible &&
          <div className='panel'>
            <div className='images-list'>
              {
                images
                .filter(this.filterOut)
                .map(image =>
                  <img
                    key={image}
                    className={ image == this.props.value? 'select': '' }
                    src={this.buildImage(image)}
                    onLoad={this.filterImage(image)}
                    onClick={this.select(image)} />
                )
              }
            </div>
          </div>
        }
      </div>
    );
  }
}