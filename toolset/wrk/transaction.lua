function init(args)
    math.randomseed(tostring(os.time()):reverse():sub(1, 7))    
end

request = function()
	wrk.method = "POST"
	wrk.path = "/transaction"
	wrk.headers["Content-Type"] = "application/json"
	wrk.body = string.format('{ "id": %d, "type": 1, "amount": %d }', math.random(1,10000), math.random(1,1000))
	return wrk.format(wrk.method, wrk.path, wrk.headers, wrk.body)
end