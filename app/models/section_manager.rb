class SectionManager
	include Singleton
	@@sections = []
	@@downcase = []
	def initializer
		print("initialize")
		File.open('catalog.txt', 'r') do |f|
			for line in f
				@@sections.append(line)
				@@downcase.append(line.downcase)
			end
		end
	end

	def get_sections(text)
		@@sections = []
		@@downcase = []
		File.open('catalog.txt', 'r') do |f|
			for line in f
				@@sections.append(line)
				@@downcase.append(line.downcase)
			end
		end
		search_text = text.downcase.gsub(' ', '')
		results = []
		for i in 0...@@downcase.length
			if @@downcase[i].include? search_text
				results.append @@sections[i]
			end
		end
		return results
	end
end
