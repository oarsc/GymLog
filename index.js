const pixels = require('image-pixels')
const jimp = require('jimp')
const sharp = require('sharp');

const fs = require('fs');

const IN = '../StudioProjects/GymLog/app/src/main/assets/previews';
const OUT = '../StudioProjects/GymLog/app/src/main/assets/masks';

async function saveTmpFile(fileName) {
  if (!fileName) return;
  await sharp(`${IN}/${fileName}`)
    .modulate({ hue: 200, saturation: 100 }) 
    .toFile(`tmp/${fileName}`);
}


async function createMask(fileName) {
  let { data, width, height } = await pixels(`tmp/${fileName}`)

  const pixelsTotal = width * height
  const group = []

  for (let i = 0; i < pixelsTotal; i++) {
    const base = i*4
    group.push([data[base], data[base+1], data[base+2], data[base+3]])
  }

  saveToFile(`${OUT}/${fileName}`, group.map(process), width, height)
}


function process([r, g, b, a]) {
  const keepPositive = val => val < 0? 0 : val

  if (a == 0) return [0, 0, 0, 0];

  const [fg, fb] = [Math.abs(g-r), Math.abs(b-r)];

  if (fg < 5 && fb < 5) return [0, 0, 0, 0];
  
  const max = Math.max(r, b);
  const effectiveGreen = keepPositive(g-max)

  return [effectiveGreen, effectiveGreen, effectiveGreen, a];
}


function saveToFile(fileName, data, width, height) {

  function RGBAToHex(r,g,b,a) {
    const toEx = (val => {
      const str = val.toString(16);
      return str.length == 1? `0${str}` : str;
    })

    return Number('0x' + toEx(r) + toEx(g) + toEx(b) + toEx(a))
  }

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = y * width + x

      data[idx].unshift(y)
      data[idx].unshift(x)
    }
  }

  new jimp(width, height, (err, image) => {
    data.forEach(([x, y, ...colors]) => {
      image.setPixelColor(RGBAToHex.apply(undefined, colors), x, y)
    });

    image.write(fileName)
  })

}


(async () => {

  if (!fs.existsSync('tmp')) {
    fs.mkdirSync('tmp')
  }

  const files = fs.readdirSync(IN);

  for (const file of files) {
    console.log(file)
    await saveTmpFile(file);
    await createMask(file);
    fs.unlinkSync(`tmp/${file}`)
  }
})()