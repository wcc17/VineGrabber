var http = require('http');
var request = require('request');
var queryString = require('querystring');
var readline = require('readline');
var fs = require('fs');

//will be used to store the vines whose download failed for redownloading later
var failedDownloadVines = [];

var rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

var getInputFunction = function getInput(callback) {
    rl.question('Enter username to download likes from: ', function(searchUsername) {
        rl.question('Is the profile private?: ', function(yesOrNo) {
            if(yesOrNo == 'Y' || yesOrNo == 'y') {
                getUserAndPass(callback, searchUsername);
            } else {
                callback(null, null, searchUsername);
            }
        })
    })

    // callback('christian.curry17@gmail.com', 'oops', 'VicCherolis');
    //callback(null, null, 'VicCherolis');
}

var getUserAndPass = function(callback, searchUsername) {
    rl.question("Username to login with: ", function(username) {
        rl.question("Password to login with: ", function(password) {
            callback(username, password, searchUsername);
        });
    });
}

//POST https://api.vineapp.com/users/authenticate
//username=xxx@example.com
//password=xxx
var executeLoginRequest = function(username, password, searchUsername) {

    if(username != null && password != null) {
        //set up form data
        var form = {
            username : username,
            password : password
        }

        //called once the request returns
        var onLoginResponse = function(err, httpResponse, body) {
            var data = body['data'];
            var userId = data['userId'];
            var sessionId = data['key'];

            console.log(sessionId);

            //TODO: check if data['success'] or else pass an error to the login emitter

            getUser(searchUsername, sessionId);
        }

        //make the request
        request({
            headers: {
                'Content-Type' : 'application/x-www-form-urlencoded'
            },
            uri: 'https://api.vineapp.com/users/authenticate',
            qs: form,
            method: 'POST',
            json: true
        }, onLoginResponse);
    } else {
        getUser(searchUsername, null);
    }

}

var getUser = function(searchUsername, sessionId) {
    //TODO: replace VicCherolis with user input
    var uri = 'https://api.vineapp.com/users/search/' + searchUsername;

    var onGetUserResponse = function(err, httpResponse, body) {
        //not sure why I was getting errors with json : true in request, but parsing here to remedy that
        var json = JSON.parse(body);
        var data = json.data;
        var records = data['records'];

        //returned in a weird format, maybe some users have more than one record
        records = records[0];

        var userId = records['userId'];
        getLikes(userId, sessionId, 1, []);
    }

    request({
        headers: {
            'Content-Type' : 'application/javascript',
            'session-id' : sessionId //TODO: test this out with random user timelines to see if private users return errors
        },
        uri: uri,
        method: 'GET',
    }, onGetUserResponse);
};

//TODO: maybe keep global user object to store stuff like userId and sessionId
var getLikes = function recursiveGetLikes(userId, sessionId, page, likes) {
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

        if(nextPage != null) {
        // if(nextPage <= 10) {
            recursiveGetLikes(userId, sessionId, nextPage, likes);
        } else {
            processVines(likes);
        }

    }

    request({
        headers: {
            'Content-Type' : 'application/json',
            'session-id' : sessionId //TODO: test this out with random user timelines to see if private users return errors
        },
        url: uri,
        qs: form,
        json: true
    }, onGetLikesResponse);
};

var getVineString = function(vine) {
    var string =
        + vine.likeIndex
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
            likeIndex : i,
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

        fs.appendFile('full-list-vines.txt', getVineString(processedVine), function(error) {
            if(error) {
                console.log('error writing to file');
            }
        });
    }

    // downloadVines(processedVines, 0);
}

var downloadVines = function downloadVine(processedVines, index) {
    if(index < processedVines.length) {
        console.log('Downloading vine # ' + processedVines[index].likeIndex);

        var fileName = processedVines[index].likeIndex
            + ' - '
            + processedVines[index].username + ' - '
            + processedVines[index].created
            + '.mp4';

        onReadyForDownload(processedVines, index, fileName);

    } else {
        if(failedDownloadVines.length == 0) {
            console.log('attempting redownload of failed vines');
            var vines = failedDownloadVines;
            failedDownloadVines = [];
            downloadVines(vines, 0);
        }

        process.exit();
    }
}

var onReadyForDownload = function(processedVines, index, videoFileName) {
    videoFileName = videoFileName.replace(/\//g, '-');
    videoFileName = videoFileName.replace(/:/g, '-');

    //TODO: test out private profile urls to see if they can be downloaded without session id
    request(processedVines[index].videoUrl)
        .on('response', function(response) {
            console.log('Beginning download for vine # ' + index);
        })
        .on('error', function(err) {
            console.log('Error downloading vine # ' + index);
            downloadVines(processedVines, index);
        })
        .on('socket', function(socket) {
            //TODO: magic number
            socket.setTimeout(30000);
            socket.on('timeout', function() {
                console.log('Retrying download of vine # ' + index);
                socket.end();
                socket.destroy();
                //downloadVines(processedVines, index);

                console.log('adding vine to retry list');
                failedDownloadVines.push(processedVines[index]);
            })
        })
        .on('end', function() {
            console.log('Finished download for vine # ' + index);
            index = index + 1;
            downloadVines(processedVines, index);
        })
        .pipe(fs.createWriteStream(videoFileName));
}

getInputFunction(executeLoginRequest);
