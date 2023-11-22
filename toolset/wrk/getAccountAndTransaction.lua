function init(args)
    math.randomseed(tostring(os.time()):reverse():sub(1, 7))    
end

request = function()
	wrk.path = string.format("/getAccountAndTransaction/%d", math.random(1,10000))
	return wrk.format(nil, wrk.path)
end