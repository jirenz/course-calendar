class CoursesController < ApplicationController
	def index
		if params[:search]
			@keyword = params[:search]
			@results = SectionManager.instance.get_sections @keyword
			@pages = (@results.length / 100 + 1).to_i
			@count = @results.length
			@page = params[:page].to_i
			if @page == nil || @page > @pages || @page <= 0
				@page = 1
			end
			range = (@page - 1)*100...[@results.length, (@page)* 100].min
			@results = @results[range]
		end
	end

	def search
	end
end
