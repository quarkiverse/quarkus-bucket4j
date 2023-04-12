import { LitElement, html, css} from 'lit';
import { pages } from 'build-time-data';
import 'qwc/qwc-extension-link.js';

const NAME = "Bucket4j";
export class QwcBucket4jCard extends LitElement {

    static styles = css`
      .identity {
        display: flex;
        justify-content: flex-start;
      }

      .description {
        padding-bottom: 10px;
      }

      .logo {
        padding-bottom: 10px;
        margin-right: 5px;
      }

      .card-content {
        color: var(--lumo-contrast-90pct);
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 2px 2px;
        height: 100%;
      }

      .card-content slot {
        display: flex;
        flex-flow: column wrap;
        padding-top: 5px;
      }
    `;

    static properties = {
        extensionName: {attribute: true},
        description: {attribute: true},
        guide: {attribute: true},
        namespace: {attribute: true},
    };

    constructor() {
        super();
        if(!this.extensionName){
            this.extensionName = NAME;
        }
    }

    connectedCallback() {
        super.connectedCallback();
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="identity">
                <div class="logo">
                    <img src="data:image/png;base64,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"
                                       alt="${NAME}" 
                                       title="${NAME}"
                                       width="32" 
                                       height="32">
                </div>
                <div class="description">${this.description}</div>
            </div>
            ${this._renderCardLinks()}
        </div>
        `;
    }

    _renderCardLinks(){
        return html`${pages.map(page => html`
                            <qwc-extension-link slot="link"
                                namespace="${this.namespace}"
                                extensionName="${this.extensionName}"
                                iconName="${page.icon}"
                                displayName="${page.title}"
                                staticLabel="${page.staticLabel}"
                                dynamicLabel="${page.dynamicLabel}"
                                streamingLabel="${page.streamingLabel}"
                                path="${page.id}"
                                ?embed=${page.embed}
                                externalUrl="${page.metadata.externalUrl}"
                                webcomponent="${page.componentLink}" >
                            </qwc-extension-link>
                        `)}`;
    }

}
customElements.define('qwc-bucket4j-card', QwcBucket4jCard);