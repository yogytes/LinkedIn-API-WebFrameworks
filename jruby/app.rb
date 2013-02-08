require "rubygems"
require "bundler/setup"
require "sinatra"
require "sinatra/reloader"
require "linkedin"
require File.dirname(__FILE__) + "/helpers.rb"
require File.dirname(__FILE__) + "/environment.rb"

enable :sessions

get "/" do
  redirect '/auth' if !logged_in?
  @profile = profile
  @connections = connections
  erb :index
end

get "/auth" do
  client = LinkedIn::Client.new(settings.api, settings.secret)
  request_token = client.request_token(:oauth_callback => "http://#{request.host}:#{request.port}/auth/callback")
  session[:rtoken] = request_token.token
  session[:rsecret] = request_token.secret

  redirect client.request_token.authorize_url
end

get "/auth/logout" do
   session[:atoken] = nil
   redirect "/"
end

get "/auth/callback" do
  client = LinkedIn::Client.new(settings.api, settings.secret)
  if session[:atoken].nil?
    pin = params[:oauth_verifier]
    atoken, asecret = client.authorize_from_request(session[:rtoken], session[:rsecret], pin)
    session[:atoken] = atoken
    session[:asecret] = asecret
  end
  redirect "/"
end
