function init(args)
    math.randomseed(tostring(os.time()):reverse():sub(1, 7))    
end

request = function()
	wrk.method = "POST"
	wrk.path = "/createAccount"
	wrk.headers["Content-Type"] = "application/json"
	wrk.body = string.format('{ "name": "test%d", "balance": 0}', math.random(1,10000))
	return wrk.format(wrk.method, wrk.path, wrk.headers, wrk.body)
end