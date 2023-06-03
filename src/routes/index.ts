import { Router } from "express";

export const router = Router();

/* GET home page. */
router.get('/', (req, res, next) =>
  res.render('index', { title: 'Express' })
);
