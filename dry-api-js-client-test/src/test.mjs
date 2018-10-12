import {ApiConnector} from 'dry-api-js-client'

const api = new ApiConnector("http://localhost:8080/api")

async function run() {

    let act = 0

    for (let i = 0;i<50;++i) {
        const output = await api.execute("math/integer/add", {a: act, b: 42})
        act = output.res
        console.log(`${i}: ${act}`)
    }
}

run().then(()=>{
    console.log("done")    
})