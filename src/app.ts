import express from "express";
import { NextFunction, Request, Response } from "express";

import HttpClientError from './model/error'

import createError from 'http-errors';
import path from 'path';
import cookieParser from 'cookie-parser';
import lessMiddleware from 'less-middleware';
import logger from 'morgan';

// routers
import { router as indexRouter } from './routes/index';
import { router as oldIndexRouter } from './routes/old-index';
import { router as readerRouter } from './routes/reader';
import { router as converterRouter } from './routes/converter';

export const app = express();
const rootDir = path.join(__dirname, '..')

//global.DEVELOPMENT = app.get('env') === 'development';

// view engine setup
app.set('views', path.join(rootDir, 'private/views'));
app.set('view engine', 'pug');

app.use(logger('dev'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(lessMiddleware(path.join(rootDir, 'private'), {
  dest: path.join(rootDir, 'public')
}));
app.use(express.static(path.join(rootDir, 'public')));

app.use('/',        indexRouter);
app.use('/old',     oldIndexRouter);
app.use('/read',    readerRouter);
app.use('/convert', converterRouter);

// catch 404 and forward to error handler
app.use((req: Request, res: Response, next: NextFunction) => {
  next(createError(404));
});

// error handler
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status((err instanceof HttpClientError? err.status : 500) ?? 500);
  res.render('error');
});
