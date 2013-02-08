# Helper methods available in both controller and views
helpers do

  def logged_in?
    !session[:atoken].nil?
  end

  def profile
    linkedin_client.profile(:fields => %w(first-name last-name headline positions summary three-current-positions picture-url)) if logged_in?
  end

  def connections
    linkedin_client.connections["all"].sort_by{ |e| e.first_name } if logged_in?
  end

  private
  def linkedin_client
    client = LinkedIn::Client.new(settings.api, settings.secret)
    client.authorize_from_access(session[:atoken], session[:asecret])
    client
  end
end
