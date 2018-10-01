const {DryApi} = require('dry-api-js-client')


const api = new DryApi("https://testmaster.navigo3.com")
api.executeAsync("sample/test", {test: 42})