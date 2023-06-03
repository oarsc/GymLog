import HttpClientError from "./model/error";

type ErrorCallback = (error: HttpClientError) => void;

export function sendError(message: string | HttpClientError, status: number, callback: ErrorCallback) {
  callback(getError(message, status));
}

export function handleError(error: string | HttpClientError | undefined | null, status: number, rejectFunction: ErrorCallback, resolve: () => void) {
  if (error)
    sendError(error, status, rejectFunction);
  else
    resolve();
}

export function throwError(message: string | HttpClientError, status: number): never {
  throw getError(message,status);
}

export function getError(message: string | HttpClientError, status: number) : HttpClientError {
  const error = typeof message == 'string' ? new HttpClientError(message) : message;
  error.status = status;
  return error;
}
