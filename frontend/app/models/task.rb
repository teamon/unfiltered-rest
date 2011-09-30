class Task < Base
  schema do
    string :name
    integer :priority
    text :content
  end
end
