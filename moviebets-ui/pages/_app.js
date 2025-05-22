import 'bootstrap/dist/css/bootstrap.css';
import buildClient from '../api/build-client';
import Header from '../components/header';
import {KeycloakProvider} from '../auth/provider/KeycloakProvider';
import { MessageContextProvider } from '../provider/MessageContextProvider';

const AppComponent = ({ Component, pageProps }) => {
  return (
    <KeycloakProvider>
        <MessageContextProvider>
          <Header />
          <div className="container">
            <Component {...pageProps} />
          </div>
        </MessageContextProvider>
    </KeycloakProvider>
  );
};

AppComponent.getInitialProps = async (appContext) => {
  const client = buildClient(appContext.ctx);

  const data = {}

  let pageProps = {};
  if (appContext.Component.getInitialProps) {
    pageProps = await appContext.Component.getInitialProps(appContext.ctx, client);
  }

  return {
    pageProps,
    ...data,
  };
};

export default AppComponent;