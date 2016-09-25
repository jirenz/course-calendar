require 'test_helper'

class RefsControllerTest < ActionController::TestCase
  setup do
    @ref = refs(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:refs)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create ref" do
    assert_difference('Ref.count') do
      post :create, ref: {  }
    end

    assert_redirected_to ref_path(assigns(:ref))
  end

  test "should show ref" do
    get :show, id: @ref
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @ref
    assert_response :success
  end

  test "should update ref" do
    patch :update, id: @ref, ref: {  }
    assert_redirected_to ref_path(assigns(:ref))
  end

  test "should destroy ref" do
    assert_difference('Ref.count', -1) do
      delete :destroy, id: @ref
    end

    assert_redirected_to refs_path
  end
end
