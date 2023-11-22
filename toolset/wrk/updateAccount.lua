function init(args)
    math.randomseed(tostring(os.time()):reverse():sub(1, 7))    
end

request = function()
	wrk.method = "POST"
	wrk.path = "/updateAccount"
	wrk.headers["Content-Type"] = "application/json"
	wrk.body = string.format('{ "id": %d, "name": "test%d" }', math.random(1,10000), math.random(1,10000))
	return wrk.format(wrk.method, wrk.path, wrk.headers, wrk.body)
end