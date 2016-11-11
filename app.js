var http = require('http');
var request = require('request');
var queryString = require('querystring');
var readline = require('readline');
var fs = require('fs');

// var rl = readline.createInterface({
//     input: process.stdin,
//     output: process.stdout
// });
//
var getInputFunction = function getInput(callback) {
//     rl.question("Username: ", function(username) {
//         rl.question("Password: ", function(password) {
//             callback(username, password);
//         });
//     });
    callback(null, null);
}

//POST https://api.vineapp.com/users/authenticate
//username=xxx@example.com
//password=xxx
var executeLoginRequest = function(username, password) {
    //set up form data
    // var form = {
    //     username : username,
    //     password : password
    // }
    //
    // //called once the request returns
    // var onLoginResponse = function(err, httpResponse, body) {
    //     var data = body['data'];
    //     var userId = data['userId'];
    //     var key = data['key'];
    //
    //     //TODO: check if data['success'] or else pass an error to the login emitter
    //
    //     getUser(userId, key);
    // }
    //
    // //make the request
    // request({
    //     headers: {
    //         'Content-Type' : 'application/x-www-form-urlencoded'
    //     },
    //     uri: 'https://api.vineapp.com/users/authenticate',
    //     qs: form,
    //     method: 'POST',
    //     json: true
    // }, onLoginResponse);

    getUser();
}

// var getUser = function(userId, key) {
var getUser = function() {
    //TODO: replace VicCherolis with user input
    var uri = 'https://api.vineapp.com/users/search/' + 'VicCherolis';

    var onGetUserResponse = function(err, httpResponse, body) {
        //not sure why I was getting errors with json : true in request, but parsing here to remedy that
        var json = JSON.parse(body);
        var data = json.data;
        var records = data['records'];

        //returned in a weird format, maybe some users have more than one record
        records = records[0];

        var userId = records['userId'];
        getLikes(userId, 1, []);
    }

    request({
        headers: {
            'Content-Type' : 'application/javascript',
            //vine-session-id: key TODO: test this out with random user timelines to see if private users return errors
        },
        uri: uri,
        method: 'GET',
    }, onGetUserResponse);
};

var getLikes = function recursiveGetLikes(userId, page, likes) {
    console.log('Getting likes for page ' + page + ' of likes for userId ' + userId);

    var uri = 'https://api.vineapp.com/timelines/users/' + userId + '/likes';

    //set up form data
    var form = {
        page : page,
    }

    var onGetLikesResponse = function(err, httpResponse, body) {
        var data = body['data'];
        var records = data['records'];

        for(var i = 0; i < records.length; i++) {
           likes.push(records[i]);
        }

        var nextPage = data['nextPage'];

        // if(nextPage != null) {
        if(nextPage <= 5) {
            recursiveGetLikes(userId, nextPage, likes);
        } else {
            processVines(likes);
        }

    }

    request({
        headers: {
            'Content-Type' : 'application/json',
            //vine-session-id: key TODO: test this out with random user timelines to see if private users return errors
        },
        url: uri,
        qs: form,
        json: true
    }, onGetLikesResponse);
};

var getVineString = function(index, vine) {
    var string =
        index + ': '
        + '\n' + 'avatarUrl: ' + vine.avatarUrl
        + '\n' + 'created: ' + vine.created
        + '\n' + 'description: ' + vine.description
        + '\n' + 'likes: ' + vine.likes
        + '\n' + 'loops: ' + vine.loops
        + '\n' + 'username: ' + vine.username
        + '\n' + 'venueAddress: ' + vine.venueAddress
        + '\n' + 'venueCity: ' + vine.venueCity
        + '\n' + 'venueCountryCode: ' + vine.venueCountryCode
        + '\n' + 'venueName: ' + vine.venueName
        + '\n' + 'venueState: ' + vine.venueState
        + '\n' + 'videoUrl: ' + vine.videoUrl
        + '\n\n';

    return string;
}

var processVines = function(vines) {
    var processedVines = [];

    fs.createWriteStream('full-list-vines.txt', processedVines, function(err) {
        if(err) {
            console.log('error creating file');
        }

        console.log('wrote to file');
    });

    for(var i = 0; i < vines.length; i++) {
        var processedVine = {
            avatarUrl : vines[i].avatarUrl,
            created : vines[i].created,
            description : vines[i].description,
            likes : vines[i].likes.count,
            loops : vines[i].loops.count,
            username : vines[i].username,
            venueAddress : vines[i].venueAddress,
            venueCity : vines[i].venueCity,
            venueCountryCode : vines[i].venueCountryCode,
            venueName : vines[i].venueName,
            venueState : vines[i].venueState,
            videoUrl : vines[i].videoUrl
        }

        processedVines.push(processedVine);

        fs.appendFile('full-list-vines.txt', getVineString(i, processedVine), function(error) {
            if(error) {
                console.log('error writing to file');
            }
        });
    }

    downloadVines(processedVines, 0);
}

var downloadVines = function downloadVine(processedVines, index) {
    if(index < processedVines.length) {
        console.log('Downloading vine # ' + index);

        var fileName = index
            + ' - '
            + processedVines[index].username + ' - '
            + processedVines[index].created
            + '.mp4';

        onReadyForDownload(processedVines, index, fileName);

    } else {
        process.exit();
    }
}

var onReadyForDownload = function(processedVines, index, videoFileName) {
    videoFileName = videoFileName.replace(/\//g, '-');
    videoFileName = videoFileName.replace(/:/g, '-');

    request(processedVines[index].videoUrl)
        .on('response', function(response) {
            index = index + 1;
            downloadVines(processedVines, index);
        })
        .on('error', function(err) {
            console.log('ERROR DOWNLOADING VINE # ' + index);
        })
        .pipe(fs.createWriteStream(videoFileName));
}

getInputFunction(executeLoginRequest);