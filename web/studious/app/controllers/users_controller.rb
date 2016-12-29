require 'net/http'
require 'json'

class UsersController < ApplicationController

	def show
		# I need to have a user right about now...
	end

  def new
  end

  def create
  	name = params[:user][:name]
  	email = params[:user][:email]
  	pswd = params[:user][:password]
  	pswd_conf = params[:user][:password_confirmation]

  	# Make sure that the length of the name is less than 255 characters
  	if name.length > 255
  		flash.now[:danger] = "Name should be less than 256 characters!"
  		render 'new'

  	elsif !(email =~ /\A[\w+\-.]+@[a-z\d\-]+(\.[a-z\d\-]+)*\.[a-z]+\z/i)
  		flash.now[:danger] = "You entered an invalid email!"
  		render 'new'
  	

  	# The lenght of the email should be longer than 6 characters
  	elsif pswd.length < 6
  		flash.now[:danger] = "Password should be 6 characters or longer!"
  		render 'new'
  	

  	# Make sure the password and its confirmation are the same
  	elsif pswd != pswd_conf
  		flash.now[:danger] = "Passwords do not match!"
  		render 'new'

  	# Create a new user!
  	else
      # ********************* CHANGE THE URL HERE ************************
      url = 'https://api.spotify.com/v1/search?type=artist&q=tycho'
      uri = URI(url)
      response = Net::HTTP.get(uri)
      json_response = JSON.parse(response)
  		
  		flash.now[:success] = json_response
  		render 'new'
  	end


  end


  

end
